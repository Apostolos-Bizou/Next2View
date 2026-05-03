package com.next2me.next2view.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${security.jwt.access-token-expiry-minutes:15}")
    private int accessTokenExpiryMinutes;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtService(
            @Value("${security.jwt.private-key}") String privateKeyPem,
            @Value("${security.jwt.public-key}") String publicKeyPem
    ) throws Exception {
        this.privateKey = loadPrivateKey(privateKeyPem);
        this.publicKey  = loadPublicKey(publicKeyPem);
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        return generateAccessToken(userId, email, role, false);
    }

    public String generateAccessToken(UUID userId, String email, String role, boolean mfaVerified) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpiryMinutes * 60L);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .claim("mfaVerified", mfaVerified)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Claims validateAndParseClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            validateAndParseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserId(String token) {
        return validateAndParseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return validateAndParseClaims(token).get("role", String.class);
    }

    public boolean extractMfaVerified(String token) {
        Boolean v = validateAndParseClaims(token).get("mfaVerified", Boolean.class);
        return v != null && v;
    }

    private PrivateKey loadPrivateKey(String pem) throws Exception {
        String clean = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\n", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private PublicKey loadPublicKey(String pem) throws Exception {
        String clean = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\n", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }
}