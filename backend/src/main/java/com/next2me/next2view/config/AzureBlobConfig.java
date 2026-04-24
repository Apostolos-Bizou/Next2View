package com.next2me.next2view.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Azure Legal Vault configuration (Day 2).
 *
 * Authentication: DefaultAzureCredential (Managed Identity in Azure,
 * Azure CLI fallback for local dev). No secrets, no storage keys.
 *
 * All beans are conditional on configuration presence, so the app still
 * starts cleanly in environments where legal vault is not configured
 * (tests, local dev without Azure access).
 */
@Configuration
@Slf4j
public class AzureBlobConfig {

    @Value("${legal-vault.storage.account:}")
    private String storageAccount;

    @Value("${legal-vault.storage.container:legal-contracts}")
    private String containerName;

    @Value("${legal-vault.keyvault.uri:}")
    private String keyVaultUri;

    @Value("${legal-vault.keyvault.key-name:legal-contracts-cmk}")
    private String kekKeyName;

    /**
     * Azure AD credential used for both Blob and Key Vault auth.
     * In Azure Container Apps: picks up System-Assigned Managed Identity.
     * Locally: falls back to `az login` CLI session.
     */
    @Bean
    public DefaultAzureCredential azureCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

    /**
     * Blob container client for the legal-contracts container.
     * Returns null if legal vault is not configured — controller checks this.
     */
    @Bean
    public BlobContainerClient legalBlobContainerClient(DefaultAzureCredential cred) {
        if (storageAccount == null || storageAccount.isBlank()) {
            log.warn("Legal Vault not configured: legal-vault.storage.account is empty");
            return null;
        }
        String endpoint = "https://" + storageAccount + ".blob.core.windows.net";
        BlobServiceClient svc = new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(cred)
                .buildClient();
        BlobContainerClient container = svc.getBlobContainerClient(containerName);
        log.info("Legal Vault Blob client ready: account={}, container={}", storageAccount, containerName);
        return container;
    }

    /**
     * Cryptography client pointing at the CMK key.
     * Used to wrap/unwrap per-file DEKs (the KEK never leaves Key Vault).
     * Returns null if Key Vault is not configured.
     */
    @Bean
    public CryptographyClient legalKekCryptoClient(DefaultAzureCredential cred) {
        if (keyVaultUri == null || keyVaultUri.isBlank()) {
            log.warn("Legal Vault Key Vault not configured: legal-vault.keyvault.uri is empty");
            return null;
        }
        String keyId = keyVaultUri.replaceAll("/$", "") + "/keys/" + kekKeyName;
        CryptographyClient client = new CryptographyClientBuilder()
                .credential(cred)
                .keyIdentifier(keyId)
                .buildClient();
        log.info("Legal Vault KEK crypto client ready: keyId={}", keyId);
        return client;
    }
}
