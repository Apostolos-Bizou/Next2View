Next2View — CEO Command Center
> **Next2me Group** | Project Management & AI Reporting Platform
![Status](https://img.shields.io/badge/Status-In%20Development-blue)
![Version](https://img.shields.io/badge/Version-1.0.0-navy)
![Stack](https://img.shields.io/badge/Stack-Vue.js%20%7C%20Java%20%7C%20Python%20%7C%20Azure-informational)
![License](https://img.shields.io/badge/License-Private-red)
---
Overview
Next2View is a CEO-level project management command center for the Next2me Group of companies. It provides real-time visibility across all group entities, project categories, tasks, deadlines, financial tracking, contract management, and AI-powered reporting — all in one platform.
Companies in scope:
Polaris Financial Services
Crossworld Marine Services
WiMAS Training Center
Varship Management
Oceansoft
---
Features
Feature	Description
📊 CEO Dashboard	KPIs, category breakdown, deadlines, activity feed
📅 Gantt Timeline	Weekly timeline across all active projects
🏢 Multi-Company	Filter by company or project category
✅ Task Management	Modules → Tasks with progress tracking and blockers
📄 Contract Upload	PDF/image upload with AI-powered extraction
🤖 AI CEO Report	One-click portfolio analysis via Claude API
🔍 Contract Analysis	AI comparison: contract terms vs actual progress
🖨 Export/Print	Contract summary export to PDF
🔐 Role-Based Access	CEO / Department Head / Viewer
---
Tech Stack
```
┌─────────────────────────────────────────────────────────┐
│              NEXT2VIEW ARCHITECTURE                      │
├─────────────────────────────────────────────────────────┤
│  Frontend    │  Vue.js 3 + Vite + Pinia + Vue Router    │
│  Backend     │  Java 21 + Spring Boot 3.x               │
│  Database    │  PostgreSQL 15 (Azure Database)          │
│  File Store  │  Azure Blob Storage                      │
│  AI Services │  Python 3.11 + Azure Functions           │
│  AI Model    │  Claude API (Anthropic)                  │
│  Auth        │  JWT (RS256) + MFA (TOTP)                │
│  Secrets     │  Azure Key Vault                         │
│  Hosting     │  Azure App Service + Static Web Apps     │
│  CI/CD       │  GitHub Actions → Azure                  │
│  Monitoring  │  Azure Application Insights              │
└─────────────────────────────────────────────────────────┘
```
---
Repository Structure
```
Next2View/
├── frontend/                  # Vue.js 3 SPA
│   ├── src/
│   │   ├── components/        # Reusable UI components
│   │   ├── views/             # Dashboard, Projects, Detail, Timeline, Manage
│   │   ├── stores/            # Pinia (auth, projects, companies, tasks)
│   │   ├── services/          # Axios API client, auth service
│   │   └── router/            # Vue Router with route guards
│   └── vite.config.js
│
├── backend/                   # Spring Boot REST API
│   └── src/main/java/com/next2me/next2view/
│       ├── config/            # Security, CORS, JWT, Azure Blob
│       ├── controller/        # REST controllers
│       ├── service/           # Business logic
│       ├── repository/        # JPA repositories
│       ├── model/             # JPA entities
│       ├── dto/               # Request/Response DTOs
│       └── security/          # JWT filter, roles, MFA
│
├── ai-functions/              # Python Azure Functions
│   ├── contract_summary/      # Extract contract fields from PDF
│   ├── ceo_report/            # CEO portfolio AI report
│   ├── contract_analysis/     # Contract vs progress analysis
│   └── requirements.txt
│
├── infrastructure/            # Azure Bicep/ARM templates
│   ├── main.bicep
│   └── keyvault.bicep
│
└── .github/workflows/         # CI/CD pipelines
    ├── frontend.yml
    ├── backend.yml
    └── ai-functions.yml
```
---
Security Standards
This project enforces strict security standards. No exceptions.
🔑 Passwords: BCrypt cost factor 12
🎟 Tokens: JWT RS256 asymmetric keys | Access token 15min TTL
🍪 Storage: HttpOnly + Secure + SameSite=Strict cookies — no localStorage
📱 MFA: TOTP mandatory for CEO role
🔒 Lockout: 5 failed attempts → 15 min lockout
🗝 Secrets: Azure Key Vault only — zero secrets in code or git
📁 Files: Private Blob containers, SAS tokens 1h TTL
📋 Audit: Every data change logged with user + timestamp
🛡 Standard: OWASP Top 10 mitigated
> ⚠️ **No API keys, passwords, or connection strings are ever committed to this repository.**  
> All secrets are managed exclusively via Azure Key Vault with Managed Identity.
---
User Roles
Permission	CEO	Dept. Head	Viewer
View all companies & projects	✅	Own only	Own only
Create / edit projects	✅	Own category	❌
Update task progress	✅	Own tasks	❌
Upload contracts	✅	Own projects	❌
AI CEO Report	✅	❌	❌
AI Contract Analysis	✅	Own projects	❌
Manage companies / users	✅	❌	❌
CEO private notes	✅ only	❌	❌
---
Azure Infrastructure
Resource	SKU	Purpose
Azure App Service	B2	Spring Boot API
Azure Database for PostgreSQL	Burstable B1ms	Main database
Azure Static Web Apps	Free	Vue.js frontend
Azure Blob Storage	LRS Standard	Contract files
Azure Functions	Consumption	Python AI services
Azure Key Vault	Standard	All secrets
Azure Application Insights	Pay-as-you-go	Monitoring
---
Getting Started (Development)
Prerequisites
Java 21 (Temurin)
Node.js 20+
Python 3.11+
PostgreSQL 15 (local) or Azure Database
Azure CLI (for Key Vault access)
Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
Frontend
```bash
cd frontend
npm install
npm run dev
```
AI Functions (local)
```bash
cd ai-functions
pip install -r requirements.txt
func start
```
---
CI/CD
Every push to `main` triggers automatic deployment to Azure via GitHub Actions.
Branch	Purpose	Deploy Target
`main`	Production	Azure Production
`staging`	Pre-production	Azure Staging slot
`develop`	Integration	Azure Dev
`feature/*`	New features	Local / PR preview
`hotfix/*`	Critical fixes	Production (after review)
---
Documentation
📄 `Technical Specification v1.0` — Full architecture, DB schema, API spec, security, deployment timeline
📋 `API Reference` — OpenAPI/Swagger (available at `/api/swagger-ui` when running)
---
Status
Component	Status
HTML Prototype (v6)	✅ Complete
Vue.js Frontend	🔄 In Progress
Spring Boot API	🔄 In Progress
PostgreSQL Schema	🔄 In Progress
Python AI Functions	📋 Planned
Azure Deployment	📋 Planned
CI/CD Pipelines	📋 Planned
---
License
Private & Confidential — © 2026 Next2me Group. All rights reserved.  
This repository is private property. Unauthorized use, reproduction, or distribution is prohibited.
---
Next2View — Built for the Next2me Group CEO Command Center
