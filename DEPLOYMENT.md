# SpendWise - Render Deployment Checklist

## ✅ Pre-Deployment Checklist

### 1. Code Repository
- [ ] Push all code to GitHub
- [ ] Ensure `main` branch is up to date
- [ ] Verify `.gitignore` excludes sensitive files

### 2. Configuration Files
- [x] `render.yaml` - Render configuration
- [x] `build.sh` - Build script
- [x] `application.properties` - Local config (H2)
- [x] `application-prod.properties` - Production config (PostgreSQL)
- [x] `pom.xml` - Maven dependencies

### 3. Dependencies
- [x] PostgreSQL driver included
- [x] Spring Boot Web
- [x] Spring Security
- [x] Spring Data JPA

### 4. Static Assets
- [x] Logo files in `/images/`
- [x] Favicon configured
- [x] CSS files
- [x] JavaScript files

## 🚀 Deployment Steps

### Step 1: Push to GitHub
```bash
git init
git add .
git commit -m "Ready for Render deployment"
git branch -M main
git remote add origin <your-github-repo-url>
git push -u origin main
```

### Step 2: Create Render Account
1. Go to https://render.com
2. Sign up with GitHub
3. Authorize Render to access your repositories

### Step 3: Deploy on Render
1. Click "New +" → "Blueprint"
2. Connect your GitHub repository
3. Render will detect `render.yaml` automatically
4. Click "Apply"
5. Wait for deployment (5-10 minutes)

### Step 4: Verify Deployment
- [ ] Application is running
- [ ] Database is connected
- [ ] Static files are loading
- [ ] API endpoints are working
- [ ] Login/authentication works

## 🔧 Environment Variables (Auto-configured)

Render automatically sets:
- `SPRING_PROFILES_ACTIVE=prod`
- `JDBC_DATABASE_URL` (from PostgreSQL)
- `PORT` (assigned by Render)
- `JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m`

## 📊 Post-Deployment

### Monitor Your Application
- View logs: Render Dashboard → Your Service → Logs
- Check metrics: Dashboard → Metrics
- Health check: `https://your-app.onrender.com/`

### Database Management
- Access PostgreSQL: Render Dashboard → Database → Connect
- Connection string available in dashboard
- Use pgAdmin or DBeaver for GUI access

## 🐛 Troubleshooting

### Build Fails
```bash
# Locally test the build
./build.sh
```

### Application Won't Start
- Check logs in Render Dashboard
- Verify Java version (21)
- Ensure PostgreSQL is running

### Database Connection Issues
- Verify `JDBC_DATABASE_URL` is set
- Check PostgreSQL status in Render
- Review connection logs

### Static Files Not Loading
- Verify files are in `src/main/resources/static/`
- Check file paths in HTML
- Clear browser cache

## 📝 Important URLs

After deployment, your app will be available at:
- **Application**: `https://spendwise.onrender.com`
- **API Base**: `https://spendwise.onrender.com/api`
- **Health Check**: `https://spendwise.onrender.com/`

## 🔐 Security Notes

- Never commit database credentials
- Use environment variables for sensitive data
- Enable HTTPS (automatic on Render)
- Keep dependencies updated

## 💡 Tips

1. **Free Tier Limitations**:
   - App sleeps after 15 minutes of inactivity
   - First request after sleep takes ~30 seconds
   - 750 hours/month free

2. **Performance**:
   - Use connection pooling
   - Enable caching where appropriate
   - Optimize database queries

3. **Monitoring**:
   - Set up health checks
   - Monitor error logs
   - Track response times

## 📞 Support

- Render Docs: https://render.com/docs
- Spring Boot Docs: https://spring.io/projects/spring-boot
- GitHub Issues: Create an issue in your repository

---

✅ Your SpendWise application is now ready for Render deployment!
