# Local Dev Scripts

These scripts make starting/stopping the local dev environment painless.

## Usage

From the project root (`C:\Users\akage\Next2View`):

```powershell
# Start backend (port 8080)
.\scripts-local\start-backend.ps1

# Start frontend (port 5173) - in a SEPARATE terminal
.\scripts-local\start-frontend.ps1

# Stop everything
.\scripts-local\stop-all.ps1
```

## What each does

- `start-backend.ps1`: kills any old process on 8080, sources `~/.next2view-local/setup-local-env.ps1`, runs `mvn spring-boot:run`
- `start-frontend.ps1`: kills any old process on 5173, runs `npm run dev` in `frontend/`
- `stop-all.ps1`: kills processes on 8080 and 5173

## Env vars source

All local env vars (JWT keys, DB credentials, Azure placeholders) live in:
`$env:USERPROFILE\.next2view-local\setup-local-env.ps1`

This file is OUTSIDE the repo and never committed.
