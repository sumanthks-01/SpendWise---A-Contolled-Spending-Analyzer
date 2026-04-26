# SpendWise - Smart Expense Tracker

![SpendWise Logo](Logo.png)

A modern, full-stack expense tracking application built with Spring Boot and vanilla JavaScript.

## Features

- 📊 Track subscriptions across multiple currencies
- 💱 Real-time currency conversion
- 📈 Visual spending analytics with charts
- 🔔 Upcoming renewal alerts
- 💰 Budget limit tracking
- 🌐 Multi-currency support
- 🔐 Secure authentication

## Tech Stack

**Backend:**
- Java 21
- Spring Boot 3.4.1
- Spring Security
- Spring Data JPA
- PostgreSQL (Production) / H2 (Development)

**Frontend:**
- Vanilla JavaScript
- Chart.js
- Roboto Condensed Font
- Glass Morphism UI

## Local Development

### Prerequisites
- Java 21 or higher
- Maven 3.6+

### Running Locally

1. Clone the repository:
```bash
git clone https://github.com/sumanthks-01/SpendWise---A-Contolled-Spending-Analyzer
cd SpendWise
```

2. Build the project:
```bash
mvn clean package
```

3. Run the application:
```bash
java -jar target/SpendWise.jar
```

4. Open your browser and navigate to:
```
http://localhost:8080
```

The application uses H2 in-memory database for local development.

## Deployment to Render

### Prerequisites
- GitHub account
- Render account (free tier available)

### Deployment Steps

1. **Push your code to GitHub:**
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin <your-github-repo-url>
git push -u origin main
```

2. **Create a new Web Service on Render:**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Render will automatically detect the `render.yaml` configuration

3. **Configure Environment Variables:**
   Render will automatically set up:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `JDBC_DATABASE_URL` (from PostgreSQL database)
   - `PORT` (assigned by Render)

4. **Deploy:**
   - Click "Create Web Service"
   - Render will automatically build and deploy your application
   - The PostgreSQL database will be created automatically

5. **Access your application:**
   - Your app will be available at: `https://spendwise.onrender.com`
   - Initial deployment may take 5-10 minutes

### Database

The application automatically creates a PostgreSQL database on Render with:
- Database name: `spendwise`
- User: `spendwise`
- Plan: Free tier
- Auto-scaling: Enabled

### Monitoring

- View logs in Render Dashboard → Your Service → Logs
- Monitor health at: `https://your-app.onrender.com/`

## Configuration Files

- `render.yaml` - Render deployment configuration
- `build.sh` - Build script for Render
- `application.properties` - Local development settings (H2)
- `application-prod.properties` - Production settings (PostgreSQL)

## API Endpoints

### Subscriptions
- `GET /api/subscriptions` - Get all subscriptions
- `POST /api/subscriptions` - Create subscription
- `DELETE /api/subscriptions/{id}` - Delete subscription
- `GET /api/subscriptions/total` - Get monthly total
- `GET /api/subscriptions/upcoming` - Get upcoming renewals
- `GET /api/subscriptions/chart` - Get spending by category

### Currency
- `GET /api/currency/list` - Get supported currencies
- `GET /api/currency/convert` - Convert currency
- `GET /api/currency/rates` - Get exchange rates

### Settings
- `GET /api/settings` - Get user settings
- `POST /api/settings` - Update settings

### User
- `GET /api/user/me` - Get current user info

## Environment Variables

### Production (Render)
- `SPRING_PROFILES_ACTIVE=prod` - Activates production profile
- `JDBC_DATABASE_URL` - PostgreSQL connection string (auto-set by Render)
- `PORT` - Server port (auto-set by Render)
- `JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m` - JVM memory settings

## Troubleshooting

### Build Fails
- Ensure Java 21 is installed
- Check Maven dependencies: `mvn dependency:tree`
- Clear Maven cache: `mvn clean`

### Database Connection Issues
- Verify `JDBC_DATABASE_URL` is set correctly
- Check PostgreSQL database is running on Render
- Review logs for connection errors

### Application Won't Start
- Check Render logs for errors
- Verify port binding: Application should use `$PORT` environment variable
- Ensure all dependencies are included in `pom.xml`

## License

MIT License - feel free to use this project for learning or commercial purposes.

## Support

For issues and questions:
- Open an issue on GitHub
- Check Render documentation: https://render.com/docs

---

Made with ☕ & Java | SpendWise v2.0
