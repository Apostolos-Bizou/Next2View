# Next2View — Handoff Document

**Last updated:** 2026-05-03 (Sunday) ~20:35 EEST
**Stable tag:** `v4.6-stable-monday-ready`
**Production revision:** `next2view-api--0000142`
**Next session focus:** Refresh-token MFA bug → Secret rotation → C16 features

---

## 🟢 Current Production State (LIVE)

### Backend
- **Container App:** `next2view-api` (resource group `next2view-rg`)
- **Active revision:** `next2view-api--0000142`
- **Image:** `next2viewdevacr.azurecr.io/next2view-api:f7fe0066aec3af2c257fdb7e098097e871c7b553`
- **Profile:** `SPRING_PROFILES_ACTIVE=dev` (this is the active config in production)
- **Health:** `/api/actuator/health` returns 200 UP
- **Includes:**
  - MFA enrollment-based check (no step-up required)
  - Access token TTL: 60 minutes
  - JWT validation logging at WARN level

### Frontend
- **URL:** https://www.next2view.com (200 OK)
- **Hosted on:** Azure Static Web Apps
- **Includes:**
  - 401-only refresh interceptor (403 passes through)
  - MFA error detection in api interceptor

### Database
- **Host:** `next2view-dev-pg.postgres.database.azure.com`
- **Database:** `next2view_dev`
- **Schema:** Flyway migrations through V22, next is V23
- **No pending changes today**

---

## 📊 What Happened Today (2026-05-03)

### Problem Reported
Σήμος Βαριάς (CEO of Crossworld Marine) reported:
1. Application kept logging him out while working
2. MFA verification banner appeared on Legal file uploads even though MFA was enabled

### Root Causes Discovered (in order)
1. **MFA check was wrong by design.** `requireMfaForFiles()` checked for `MFA_VERIFIED` authority on JWT, but no step-up MFA flow existed in login. Non-CEO users with MFA enabled could never access legal files.

2. **Access token TTL was 15 minutes.** Production runs `SPRING_PROFILES_ACTIVE=dev`, and `application-dev.yml` had stale `access-token-expiry-minutes: 15`. Base `application.yml` had 60min, but it was being overridden.

3. **CI/CD race condition.** `backend.yml` (CI workflow) was dual-purpose: built/tested AND deployed on every develop push. Combined with `backend-deploy.yml` on main pushes, this caused `ContainerAppOperationInProgress` errors when both ran close together.

4. **Silent JWT failures.** `JwtService.isValid()` swallowed `JwtException` as `log.debug` — invisible in production WARN log level.

### Fixes Deployed Today

| # | Commit | Revision | Description |
|---|---|---|---|
| 1 | `7d6a116` | `0000137` | MFA enrollment-based check (replaced step-up authority lookup) |
| 2 | `5f0494d` | `0000139` | Token TTL 15→60 min in dev profile |
| 3 | `99c4587` | `0000140` | Repo cleanup: 7 .bak files + .gitignore extension |
| 4 | `7cb0cea` | `0000142` | JWT validation logging at WARN level |
| 5 | `19ab8a1` | (no deploy) | CI workflow cleanup (removed deploy steps from backend.yml) |

### Tags Created
- `v4.5-mfa-and-ttl-stable` — after fixes 1+2 (user-verified working)
- `v4.6-stable-monday-ready` — final state for Monday

---

## ⏭️ Next Session — Recommended Order

### 1. Refresh-token MFA Bug (LOW risk, 30 min)
**File:** `backend/src/main/java/com/next2me/next2view/service/AuthService.java` (line ~85)

**Bug:**
```java
String newAccess = jwtService.generateAccessToken(
        user.getId(), user.getEmail(), user.getRole().name(),
        user.getMfaEnabled());  // WRONG: should be `false`
```

**Fix:** Change `user.getMfaEnabled()` to `false`. The `mfaVerified` claim should be `true` ONLY when the user has completed a TOTP challenge in this session, not just because they have MFA enrolled.

**Impact:** Currently not breaking anything (the new enrollment-based check uses `user.mfaEnabled` from DB, not the JWT claim). But it is wrong-by-design and will create issues when the proper step-up flow is implemented.

### 2. Secret Rotation (MEDIUM risk, 45 min)
Secrets exposed in terminal output today during debugging:
- DB_PASSWORD
- JWT_PRIVATE_KEY
- ANTHROPIC_API_KEY
- AZURE_STORAGE_KEY

**Plan:**
1. Generate new secrets (especially JWT keypair via openssl)
2. Update Container App env vars + Key Vault references
3. **Force users to re-login on Monday morning** (warn them via Slack/email beforehand)
4. Old refresh tokens should be invalidated via `REVOKE` table cleanup or wait for natural expiry

**Window:** Best Monday morning before users start, or weekend.

### 3. CEO Role Review for Σήμος (DATA decision, no code)
**Question:** Is `Role.CEO` correct for `s.varias@crossworldmarine.com`?
- Currently: full CEO bypass on all permission checks
- Memory describes him as `CEO of Crossworld Marine` (one of multiple companies)
- Should he be `DEPT_HEAD` scoped to Crossworld Marine instead?

**Decision needed before any change.** This is a data update, not code.

### 4. C16 Features (from existing backlog)
- Notifications split-panel (desktop 40/60, mobile full-swap)
- Legal Vault Day 3-5 (frontend refactor + go-live)
- Gantt multi-zoom

---

## 🚨 Things to Know Before Doing Anything

### Working PowerShell Patterns (proven today)
1. **DO NOT** use PowerShell here-strings (`@'...'@`) for JS code with `{}` and quotes — escaping breaks
2. **DO** use `[System.IO.File]::ReadAllText` + `.Replace(oldStr, newStr)` + `WriteAllText` with `UTF8NoBom`
3. **DO** use line-by-line patching when whole-string match fails (CRLF issues)
4. **ALWAYS** verify with brace-counting that replacements left valid syntax
5. **ALWAYS** `mvn compile -q -DskipTests` before any commit/push

### Deployment Workflow (CORRECT)
```r
develop push (backend/** changes)  ->  Backend CI/CD (build + test ONLY, no deploy)
main push (backend/** changes)     ->  Backend Deploy (build + push image + deploy)
develop push (frontend/** changes) ->  Frontend CI (build only)
main push (frontend/** changes)    ->  Frontend Deploy (build + Azure Static deploy)
```

**Path filters work** — `.github/workflows/*.yml` changes do NOT trigger any deploy.

### Things That Will Break If You Touch Them
- **`application-dev.yml`:** This is the ACTIVE production config. Don't break it.
- **`SPRING_PROFILES_ACTIVE=dev`:** ENV var on Container App. If you change to `prod` or remove it, you'll be running the base profile which is different.
- **`PermissionEvaluator.requireMfaForFiles`:** Now uses `user.mfaEnabled`. If you reintroduce step-up checks, the entire Legal Vault breaks for non-CEO users until step-up flow exists.

---

## 📞 Quick References

| Resource | Location |
|---|---|
| Repo | github.com/Apostolos-Bizou/Next2View |
| Production frontend | https://www.next2view.com |
| Production API | next2view-api.politefield-8c14abcc.northeurope.azurecontainerapps.io |
| Container Registry | next2viewdevacr.azurecr.io |
| Key Vault | next2view-dev-kv |
| Database | next2view-dev-pg.postgres.database.azure.com |
| Local code | C:\Users\akage\Next2View |

---

## 🎯 Tags History (newest first)
- `v4.6-stable-monday-ready` <- current stable, Monday-ready
- `v4.5-mfa-and-ttl-stable` <- post-MFA fixes
- `v4.4-mfa-enforcement-complete` <- original MFA enforcement attempt
- `v4.3-sse-realtime-push-complete`
- `v4.3-pre-mfa-enforcement`
- `v4.2-activity-cards-polished`
- `v4.1-i18n-gaps-complete`
- `v4.0-task-granular-logging-deployed`

To rollback to any stable point: `git checkout v4.6-stable-monday-ready`

**WARNING:** Force-push to main only in emergencies; better to revert via merge commit.
