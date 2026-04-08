// Key Vault secrets setup
// Run once manually to populate secrets

param keyVaultName string
param dbPassword string
param jwtPrivateKey string
param jwtPublicKey string

resource keyVault 'Microsoft.KeyVault/vaults@2023-02-01' existing = {
  name: keyVaultName
}

resource dbPasswordSecret 'Microsoft.KeyVault/vaults/secrets@2023-02-01' = {
  parent: keyVault
  name: 'db-password'
  properties: { value: dbPassword }
}

resource jwtPrivateKeySecret 'Microsoft.KeyVault/vaults/secrets@2023-02-01' = {
  parent: keyVault
  name: 'jwt-private-key'
  properties: { value: jwtPrivateKey }
}

resource jwtPublicKeySecret 'Microsoft.KeyVault/vaults/secrets@2023-02-01' = {
  parent: keyVault
  name: 'jwt-public-key'
  properties: { value: jwtPublicKey }
}

resource storageConnectionSecret 'Microsoft.KeyVault/vaults/secrets@2023-02-01' = {
  parent: keyVault
  name: 'storage-connection-string'
  properties: { value: 'placeholder-set-after-storage-creation' }
}