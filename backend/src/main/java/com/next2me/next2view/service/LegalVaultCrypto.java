package com.next2me.next2view.service;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Pure cryptographic operations for the Legal Vault.
 *
 * Format (all fields stored separately in DB):
 *   plaintext  -> bytes of the file as uploaded
 *   DEK        -> 32 bytes (AES-256) generated per file via SecureRandom
 *   IV         -> 12 bytes (AES-GCM standard) generated per file
 *   ciphertext -> AES-256-GCM encrypted plaintext (WITHOUT appended tag)
 *   authTag    -> 16 bytes (128-bit GCM tag) extracted separately
 *   encryptedDek -> DEK wrapped by Key Vault KEK using RSA-OAEP-256
 *   sha256     -> hex-encoded SHA-256 of plaintext (integrity check)
 *
 * Thread-safety: stateless component, safe to reuse across threads.
 */
@Component
@Slf4j
public class LegalVaultCrypto {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_TAG_BYTES = GCM_TAG_BITS / 8; // 16
    private static final int DEK_BYTES = 32; // AES-256
    private static final int IV_BYTES = 12;  // GCM standard

    private final SecureRandom rng;

    public LegalVaultCrypto() {
        try {
            this.rng = SecureRandom.getInstanceStrong();
        } catch (Exception e) {
            throw new IllegalStateException("Strong SecureRandom unavailable", e);
        }
    }

    /** Result of an encrypt operation. All fields go to DB or Blob. */
    public record EncryptionResult(
            byte[] ciphertext,
            byte[] iv,
            byte[] authTag,
            byte[] encryptedDek,
            String sha256Hex,
            String kekKeyId
    ) {}

    /**
     * Encrypts plaintext and wraps the DEK via Key Vault.
     * The DEK is cleared from memory before returning.
     */
    public EncryptionResult encrypt(byte[] plaintext, CryptographyClient kekClient) {
        if (plaintext == null) throw new IllegalArgumentException("plaintext is null");
        if (kekClient == null) throw new IllegalStateException("KEK client not configured");

        byte[] dek = new byte[DEK_BYTES];
        byte[] iv = new byte[IV_BYTES];
        rng.nextBytes(dek);
        rng.nextBytes(iv);

        try {
            // 1. SHA-256 of plaintext
            String sha256 = sha256Hex(plaintext);

            // 2. AES-256-GCM encrypt
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(dek, "AES"),
                    new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] combined = cipher.doFinal(plaintext); // ciphertext || tag

            // 3. Split combined into ciphertext + tag
            int ctLen = combined.length - GCM_TAG_BYTES;
            byte[] ciphertext = Arrays.copyOfRange(combined, 0, ctLen);
            byte[] authTag = Arrays.copyOfRange(combined, ctLen, combined.length);

            // 4. Wrap DEK via Key Vault
            WrapResult wrap = kekClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP_256, dek);
            byte[] encryptedDek = wrap.getEncryptedKey();
            String kekKeyId = wrap.getKeyId();

            return new EncryptionResult(ciphertext, iv, authTag, encryptedDek, sha256, kekKeyId);
        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        } finally {
            // Best-effort wipe of DEK from memory
            Arrays.fill(dek, (byte) 0);
        }
    }

    /**
     * Decrypts ciphertext using wrapped DEK. Verifies SHA-256 if expected hash provided.
     * Throws if GCM auth tag fails (tamper detection) or SHA-256 mismatches.
     */
    public byte[] decrypt(
            byte[] ciphertext,
            byte[] iv,
            byte[] authTag,
            byte[] encryptedDek,
            String expectedSha256Hex,
            CryptographyClient kekClient
    ) {
        if (kekClient == null) throw new IllegalStateException("KEK client not configured");

        byte[] dek = null;
        try {
            // 1. Unwrap DEK via Key Vault
            UnwrapResult unwrap = kekClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP_256, encryptedDek);
            dek = unwrap.getKey();

            // 2. Reassemble ciphertext || tag for Cipher API
            ByteBuffer buf = ByteBuffer.allocate(ciphertext.length + authTag.length);
            buf.put(ciphertext);
            buf.put(authTag);
            byte[] combined = buf.array();

            // 3. AES-256-GCM decrypt (throws if tag invalid)
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(dek, "AES"),
                    new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plaintext = cipher.doFinal(combined);

            // 4. Verify SHA-256 integrity
            if (expectedSha256Hex != null) {
                String actual = sha256Hex(plaintext);
                if (!actual.equalsIgnoreCase(expectedSha256Hex)) {
                    throw new SecurityException("SHA-256 mismatch: file integrity check failed");
                }
            }
            return plaintext;
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        } finally {
            if (dek != null) Arrays.fill(dek, (byte) 0);
        }
    }

    /** Hex-encoded SHA-256 of input. */
    public String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(data));
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
