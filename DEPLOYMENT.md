# SpendWise – Render Deployment Guide

This guide provides step-by-step instructions on how to deploy SpendWise to **Render** using the Blueprint configuration (`render.yaml`).

---

## 🏗️ Deployment Architecture
- **Web Service**: Containerized Spring Boot application built via `Dockerfile` (automatically runs on Java 21 JRE, production profile).
- **Database**: Dedicated PostgreSQL database provisioned on Render's free tier.
- **Auto-linking**: Render automatically creates the database and injects the `DATABASE_URL` connection string into the Web Service.

---

## 🚀 Step-by-Step Deployment Guide

### Step 1: Connect your GitHub Account to Render
1. Go to the [Render Dashboard](https://dashboard.render.com).
2. Register or Sign In. We recommend signing in using the **GitHub** button to automatically link your repositories.

### Step 2: Deploy the Blueprint Configuration
1. In the top-right corner of the Render Dashboard, click the dark blue **"New +"** button.
2. From the dropdown menu, select **"Blueprint"**.
3. Under the "Connect a repository" section, search for **SpendWise** and click **"Connect"**.
4. In the configuration page that appears:
   - **Blueprint Name**: Enter a name for this deployment group (e.g., `spendwise-group`).
   - **Branch**: Select `main`.
5. Under the **Environment Variables** section, Render will prompt you to fill in the parameters marked `sync: false` in `render.yaml`. Enter the following:
   
   | Environment Variable | Value/Source | Description |
   | :--- | :--- | :--- |
   | **`GOOGLE_CLIENT_ID`** | Google Cloud Console | Your Google OAuth 2.0 Client ID |
   | **`GOOGLE_CLIENT_SECRET`** | Google Cloud Console | Your Google OAuth 2.0 Client Secret |
   | **`MAIL_USERNAME`** | Your Gmail Address | The email address used to send password reset links (e.g., `yourname@gmail.com`) |
   | **`MAIL_PASSWORD`** | Google Account Security | A 16-character **Gmail App Password** (Generate via Google Account -> Security -> 2-Step Verification -> App Passwords). *Do not use your main account password.* |
   | **`APP_BASE_URL`** | Temporary Placeholder | Enter `https://spendwise.onrender.com` (we will update this once Render assigns a unique URL to our service). |

6. Click the blue **"Apply"** button at the bottom of the page. Render will begin provisioning the database (`spendwise-db`) and building the application container (`spendwise`).

---

### Step 3: Update `APP_BASE_URL` with your Unique Render URL
1. From the Render Dashboard, click on your new Web Service named **"spendwise"**.
2. Look at the top-left area of the page, below the service name. You will see a URL ending in `.onrender.com` (e.g., `https://spendwise-u8f1.onrender.com`). **Copy this URL**.
3. In the left navigation menu of the service, click on the **"Environment"** tab.
4. Scroll down to find the `APP_BASE_URL` variable.
5. Click **"Edit"**, replace the placeholder value with your copied URL, and click **"Save Changes"**.
6. Render will automatically trigger a new build/deployment to apply the updated URL.

---

### Step 4: Update Google OAuth Settings
1. Go to the [Google Cloud Console Credentials Page](https://console.cloud.google.com/apis/credentials).
2. Click on your OAuth 2.0 Client ID (the one used for SpendWise) to edit its settings.
3. Scroll down to **"Authorized JavaScript origins"** and click **"+ ADD URI"**. Paste your copied unique Render URL (e.g., `https://spendwise-u8f1.onrender.com` - *without a trailing slash*).
4. Scroll down to **"Authorized redirect URIs"** and click **"+ ADD URI"**. Paste your unique Render URL with the OAuth callback path:
   ```
   https://spendwise-u8f1.onrender.com/login/oauth2/code/google
   ```
   *(Be sure to replace `spendwise-u8f1.onrender.com` with your actual subdomain!)*
5. Click the blue **"Save"** button at the bottom of the page.

---

## 🛠️ Monitoring & Troubleshooting

### Monitoring Deploys
- Go to the **"Logs"** tab under your `spendwise` service on Render to monitor the Docker build steps and Spring Boot startup sequences.
- You should see Spring Boot startup logs concluding with a message similar to:
  ```
  INFO: Started SpendWiseApplication in X.XXX seconds
  ```

### Common Issues
- **App Startup Fails (Database error)**: Verify that the Postgres database on the Render Dashboard says "Available". Render links the database to the application automatically via the `DATABASE_URL` variable.
- **Google OAuth Redirect Mismatch**: Ensure the redirect URI in the Google Cloud Console matches the domain assigned by Render *exactly*, including `https://` and `/login/oauth2/code/google`.
- **Emails are not being sent**: Ensure `MAIL_USERNAME` is correct and `MAIL_PASSWORD` is a generated App Password, not your regular Gmail login password.
- **Cold Starts (App sleeping)**: Render's Free tier spins down web services after 15 minutes of inactivity. The first request after a cold start can take 30-50 seconds to boot up.
