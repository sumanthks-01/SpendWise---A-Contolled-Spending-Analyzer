# SpendWise – Render Deployment Guide

## Configuration Files
- `render.yaml` – Render Blueprint (web service + PostgreSQL database definition)
- `Dockerfile` – Container build (auto-activates `prod` Spring profile)
- `application.properties` – Local dev config (H2 database)
- `application-prod.properties` – Production config (PostgreSQL)

## Deployment Steps

### 1. Push to GitHub
```bash
git add .
git commit -m "feat: switch to Render deployment"
git push
```

### 2. Create a Render Account & New Blueprint
1. Go to [https://render.com](https://render.com) and sign in (or sign up) with GitHub.
2. Click **New** → **Blueprint**.
3. Select your **SpendWise** repository.
4. Render detects `render.yaml` and provisions both the **Web Service** and **PostgreSQL** database automatically.

### 3. PostgreSQL Database
The `render.yaml` Blueprint defines a free-tier PostgreSQL database named `spendwise-db`.  
Render automatically injects `DATABASE_URL` as a connection string into the Web Service — no manual action needed.

### 4. Set Environment Variables
After the Blueprint is created, go to your **Web Service → Environment** tab and fill in the following secrets (marked `sync: false` in `render.yaml`, so they are **not** committed to source):

| Variable | Value |
|----------|-------|
| `GOOGLE_CLIENT_ID` | Your Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Your Google OAuth client secret |
| `MAIL_USERNAME` | spendwise.app.noreply@gmail.com |
| `MAIL_PASSWORD` | Your Gmail App Password |
| `APP_BASE_URL` | `https://your-app.onrender.com` |

> `SPRING_PROFILES_ACTIVE` is pre-set to `prod` in `render.yaml`.  
> `DATABASE_URL` is automatically linked from the Render Postgres instance.

### 5. Update Google OAuth
In [Google Cloud Console](https://console.cloud.google.com), add your Render URL to **Authorized Redirect URIs**:
```
https://your-app.onrender.com/login/oauth2/code/google
```

### 6. Deploy
Render builds and deploys automatically on every push to your connected branch.  
Monitor progress under **Logs** in the Render Dashboard.

## Troubleshooting

- **Build fails** → Check Render build logs; ensure the Dockerfile uses `eclipse-temurin:25-jdk-alpine`
- **DB connection error** → Verify `DATABASE_URL` is linked from the Render Postgres service in Environment settings
- **OAuth not working** → Confirm the redirect URI exactly matches in Google Cloud Console
- **Email not sending** → Confirm `MAIL_USERNAME` and `MAIL_PASSWORD` are correct; use a Gmail App Password (not your account password)
- **App sleeps (free tier)** → Render free-tier web services spin down after 15 min of inactivity; the first request may take ~30 s to wake up

## Useful URLs After Deployment
- **App**: `https://your-app.onrender.com`
- **Health check**: `https://your-app.onrender.com/`
- **Render Dashboard**: `https://dashboard.render.com`
