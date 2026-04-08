// Next2View — Azure Infrastructure
// main.bicep

@description('Environment: dev, staging, prod')
param environment string = 'dev'

@description('Azure region')
param location string = resourceGroup().location

@description('App name prefix')
param appName string = 'next2view'

var prefix = '${appName}-${environment}'

// ── KEY VAULT ──
resource keyVault 'Microsoft.KeyVault/vaults@2023-02-01' = {
  name: '${prefix}-kv'
  location: location
  properties: {
    sku: { family: 'A', name: 'standard' }
    tenantId: subscription().tenantId
    enableRbacAuthorization: true
    enableSoftDelete: true
    softDeleteRetentionInDays: 7
  }
}

// ── POSTGRESQL ──
resource postgres 'Microsoft.DBforPostgreSQL/flexibleServers@2023-03-01-preview' = {
  name: '${prefix}-pg'
  location: location
  sku: { name: 'Standard_B1ms', tier: 'Burstable' }
  properties: {
    version: '15'
    administratorLogin: 'next2view_admin'
    administratorLoginPassword: keyVault::dbPasswordSecret.properties.value
    storage: { storageSizeGB: 32 }
    backup: { backupRetentionDays: 7, geoRedundantBackup: 'Disabled' }
    highAvailability: { mode: 'Disabled' }
  }
}

resource keyVault 'Microsoft.KeyVault/vaults@2023-02-01' existing = {
  name: '${prefix}-kv'
}

resource dbPasswordSecret 'Microsoft.KeyVault/vaults/secrets@2023-02-01' existing = {
  parent: keyVault
  name: 'db-password'
}

// ── STORAGE ACCOUNT ──
resource storage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: replace('${prefix}sa', '-', '')
  location: location
  sku: { name: 'Standard_LRS' }
  kind: 'StorageV2'
  properties: {
    accessTier: 'Hot'
    allowBlobPublicAccess: false
    minimumTlsVersion: 'TLS1_2'
    supportsHttpsTrafficOnly: true
  }
}

resource blobService 'Microsoft.Storage/storageAccounts/blobServices@2023-01-01' = {
  parent: storage
  name: 'default'
}

resource contractsContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-01-01' = {
  parent: blobService
  name: 'next2view-contracts'
  properties: { publicAccess: 'None' }
}

// ── APP SERVICE PLAN ──
resource appServicePlan 'Microsoft.Web/serverfarms@2023-01-01' = {
  name: '${prefix}-plan'
  location: location
  sku: { name: 'B2', tier: 'Basic' }
  properties: { reserved: true }
  kind: 'linux'
}

// ── SPRING BOOT APP SERVICE ──
resource backendApp 'Microsoft.Web/sites@2023-01-01' = {
  name: '${prefix}-api'
  location: location
  properties: {
    serverFarmId: appServicePlan.id
    siteConfig: {
      linuxFxVersion: 'JAVA|21-java21'
      alwaysOn: true
      appSettings: [
        { name: 'SPRING_PROFILES_ACTIVE', value: environment }
        { name: 'AZURE_KEYVAULT_URI', value: keyVault.properties.vaultUri }
        { name: 'WEBSITES_PORT', value: '8080' }
      ]
    }
    httpsOnly: true
  }
  identity: { type: 'SystemAssigned' }
}

// ── STATIC WEB APP (Vue.js) ──
resource staticWebApp 'Microsoft.Web/staticSites@2023-01-01' = {
  name: '${prefix}-web'
  location: 'westeurope'
  sku: { name: 'Free', tier: 'Free' }
  properties: {
    buildProperties: {
      appLocation: 'frontend'
      outputLocation: 'dist'
      appBuildCommand: 'npm run build'
    }
  }
}

// ── APPLICATION INSIGHTS ──
resource appInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: '${prefix}-insights'
  location: location
  kind: 'web'
  properties: {
    Application_Type: 'web'
    RetentionInDays: 30
  }
}

// ── OUTPUTS ──
output backendUrl string = 'https://${backendApp.properties.defaultHostName}'
output staticWebUrl string = 'https://${staticWebApp.properties.defaultHostname}'
output keyVaultUri string = keyVault.properties.vaultUri
output storageAccountName string = storage.name