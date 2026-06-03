# Architecture Overview

This document provides a high-level overview of SpendWise's architecture and major components.

## Components

- Web UI: Static frontend files served from `src/main/resources/static`.
- Backend: Spring Boot application (`SpendWiseApplication`) exposing REST endpoints.
- Persistence: Relational database configured via `DatabaseConfig`.
- Authentication: Spring Security with OAuth2 and custom `UserDetailsService`.
- Email: `EmailService` for transactional emails (password resets, notifications).

## Data Model

Key entities:
- `User` — user profile, credentials, roles.
- `Subscription` — subscription plan and billing metadata.
- `PasswordResetToken` — tokens for password reset flows.

## Deployment

- Containerized via `Dockerfile` and orchestrated by your chosen platform (Kubernetes, Railway, etc.).
- Externalize configuration with environment variables and cloud secrets.

## Scaling and Observability

- Run multiple instances behind a load balancer for scale.
- Use health checks, metrics, and centralized logging.

## Diagrams

Add system and sequence diagrams here (PlantUML or Mermaid).

