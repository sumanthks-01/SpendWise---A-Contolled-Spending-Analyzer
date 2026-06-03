# SpendWise – Railway Deployment Guide

## Configuration Files
- `railway.toml` – Railway build & deploy config
- `Dockerfile` – Container build (auto-activates `prod` profile)
- `application.properties` – Local dev config (H2 database)
- `application-prod.properties` – Production config (PostgreSQL)

## Deployment Steps

### 1. Push to GitHub
```bash
git add .
git commit -m "feat: switch to Railway deployment"
git push
```

### 2. Create Railway Project
1. Go to https://railway.app and sign in with GitHub
2. Click **New Project** → **Deploy from GitHub repo**
3. Select your SpendWise repository
4. Railway detects the `Dockerfile` and starts building automatically

### 3. Add PostgreSQL Database
Inside your Railway project, click **+ New → Database → Add PostgreSQL**.
Railway automatically injects `DATABASE_URL` into your service.

### 4. Set Environment Variables
In your Web Service → **Variables** tab, add:

| Variable | Value |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `GOOGLE_CLIENT_ID` | your Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | your Google OAuth client secret |
| `MAIL_USERNAME` | spendwise.app.noreply@gmail.com |
| `MAIL_PASSWORD` | your Gmail App Password |
| `APP_BASE_URL` | https://your-app.up.railway.app |

> `DATABASE_URL` is set automatically — do not add it manually.

### 5. Update Google OAuth
In [Google Cloud Console](https://console.cloud.google.com), add your Railway URL to **Authorized Redirect URIs**:
```
https://your-app.up.railway.app/login/oauth2/code/google
```

## Troubleshooting

- **Build fails** → Check Railway build logs; ensure Java 25 is used
- **DB connection error** → Verify `DATABASE_URL` is present in Variables
- **OAuth not working** → Check the redirect URI is added in Google Cloud Console
- **Email not sending** → Confirm `MAIL_USERNAME` and `MAIL_PASSWORD` are correct

## Useful URLs After Deployment
- **App**: `https://your-app.up.railway.app`
- **Health check**: `https://your-app.up.railway.app/`
