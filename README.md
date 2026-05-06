# SpendWise

> **Smart Subscription & Expense Tracker**

SpendWise is a premium, modern web application designed to help you track your subscriptions, manage recurring expenses, and stay within your budget. Built with Java 21, Spring Boot, and a sleek dark-themed UI, SpendWise offers multi-currency support and real-time live exchange rates.

## ✨ Features

- **Subscription Tracking**: Keep an organized list of all your recurring services (Netflix, Spotify, Gym, etc.).
- **Budget Monitoring**: Set a monthly budget and receive visual warnings when your expenses exceed the limit.
- **Multi-Currency Support**: Add subscriptions in various currencies and view them normalized to your home currency.
- **Live Exchange Rates**: Built-in currency converter powered by the free Frankfurter API.
- **Secure Authentication**: 
  - Standard Email / Password login using Spring Security and BCrypt.
  - **Google OAuth2 Integration** for seamless "Sign in with Google" access.
- **Premium UI**: A responsive, mobile-friendly interface featuring a dynamic Dark Horizon glow and Roboto Condensed typography.

## 🛠 Technology Stack

- **Backend**: Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA
- **Frontend**: Vanilla HTML/CSS/JS, Chart.js for visualizations
- **Database**: 
  - Local Development: H2 In-Memory Database (zero setup required)
  - Production/Cloud: PostgreSQL
- **Build Tool**: Maven

## 🚀 Getting Started (Local Development)

### Prerequisites
- JDK 21+
- Maven 3.8+

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/SpendWise.git
   cd SpendWise
   ```

2. **Configure Google OAuth (Optional but recommended):**
   Set your Google Client ID and Secret as environment variables before running, or add them to `application.properties`:
   ```bash
   export GOOGLE_CLIENT_ID="your-client-id"
   export GOOGLE_CLIENT_SECRET="your-client-secret"
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   The application will start with an embedded H2 database.

4. **Access the App:**
   Open your browser and navigate to: `http://localhost:8080`

## ☁️ Deployment (Render)

SpendWise is configured for seamless deployment to [Render](https://render.com/).

1. Connect your GitHub repository to Render.
2. Create a new **Web Service**.
3. Render will automatically detect the `render.yaml` and `Dockerfile`.
4. Create a **PostgreSQL** database on Render and attach it to your Web Service.
5. Add the following Environment Variables in your Render Dashboard:
   - `DATABASE_URL` (provided by Render Postgres)
   - `GOOGLE_CLIENT_ID`
   - `GOOGLE_CLIENT_SECRET`

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
