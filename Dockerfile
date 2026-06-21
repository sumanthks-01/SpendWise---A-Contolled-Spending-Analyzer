# ── Build Stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Install Maven in Alpine Linux
RUN apk add --no-cache maven

# Copy pom.xml and resolve dependencies to speed up subsequent builds (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source code and build the final executable JAR file
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Runtime Stage ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/SpendWise.jar app.jar

# Inform Docker that the container listens on port 8080 at runtime
EXPOSE 8080

# Launch the Spring Boot application under the production profile
# Render automatically injects the PORT environment variable, which overrides server.port
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Dspring.profiles.active=prod -jar app.jar"]
