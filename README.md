# 🚀 HireFlow API

> **Job Board & Recruitment Platform REST API**

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue)](https://docs.docker.com/compose/)
[![Swagger](https://img.shields.io/badge/API-Swagger%20UI-85ea2d)](http://localhost:8080/swagger-ui.html)

---

## 📌 Resume Description

> Designed and built a RESTful Job Board Platform API using Spring Boot 3.2, featuring JWT-based auth, role-based access control (Seeker / Recruiter / Admin), Jaccard similarity–based skill-gap matching, async email notifications via JavaMailSender + Spring Events, Redis caching with `@Cacheable`, paginated job search with dynamic JPA Specifications, Flyway migrations, and full Swagger/OpenAPI documentation. Containerized via Docker Compose with PostgreSQL 15, Redis, and MailHog.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    REST Controllers                      │
│   Auth | Jobs | Applications | Interviews | Profile      │
│   Skills | Notifications | Admin                         │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                   Service Layer                          │
│  AuthService | JobService | ApplicationService          │
│  MatchingService | SkillGapService | InterviewService    │
│  NotificationService                                     │
└──────┬─────────────────────┬───────────────────────────┘
       │                     │
┌──────▼──────┐    ┌─────────▼──────┐
│ JPA Repos   │    │ Spring Events  │
│ + Flyway    │    │ + Async Exec.  │
└──────┬──────┘    └─────────┬──────┘
       │                     │
┌──────▼──────┐    ┌─────────▼──────┐
│PostgreSQL 15│    │  Redis Cache   │    MailHog
└─────────────┘    └────────────────┘
```

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (JJWT) |
| Database | PostgreSQL 15 + JPA / Hibernate |
| Migrations | Flyway |
| Caching | Redis (`@Cacheable`) |
| Email | Spring Mail + MailHog (dev) |
| API Docs | Swagger / SpringDoc OpenAPI |
| Testing | JUnit 5, Mockito |
| Containerization | Docker + Docker Compose |

---

## 🔐 Authentication & Roles

| Role | Capabilities |
|---|---|
| **SEEKER** | Browse jobs, apply, manage profile, skill gap analysis |
| **RECRUITER** | Post jobs, view applicants with skill-match scores, schedule interviews, manage company |
| **ADMIN** | Platform overview, user management, force-delete jobs |

### Auth Flow
1. `POST /api/v1/auth/register` → Register account
2. `POST /api/v1/auth/login` → `{ accessToken }`
3. Include `Authorization: Bearer <accessToken>` on protected endpoints

---

## 🧠 Key Features

### Jaccard Similarity Matching
Job seekers' skills are compared to job required skills using **Jaccard similarity**:

```
score = |A ∩ B| / |A ∪ B|
```

Returns matched skills, missing required skills, and a recommendation.

### Skill Gap Analysis
`GET /api/v1/skills/gap/{jobId}` returns:
- Match percentage
- Matched skills (green)
- Missing required skills (red)
- Missing optional skills (yellow)
- Personalized recommendation

### Async Email Notifications (Spring Events)
Events are published from services and consumed asynchronously:
- `UserRegisteredEvent` → Welcome email
- `ApplicationSubmittedEvent` → Confirmation + recruiter notification
- `ApplicationStatusChangedEvent` → Status update email
- `InterviewScheduledEvent` → Interview invite email

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local dev only)
- Maven 3.9+

### Run with Docker Compose (Recommended)

```bash
# Clone the repo
git clone https://github.com/pranavmisal007-ctrl/hireflow-api.git
cd hireflow-api

# Start all services (MySQL, Redis, MailHog, App)
docker-compose up -d

# View logs
docker-compose logs -f app
```

The API will be available at: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html  
MailHog UI: http://localhost:8025  

### Run Locally

```bash
# Start infrastructure only
docker-compose up -d mysql redis mailhog

# Copy env file
cp .env.example .env

# Run the app
mvn spring-boot:run
```

---

## 📡 API Endpoints Summary

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register (SEEKER/RECRUITER) |
| POST | `/api/v1/auth/login` | Login → JWT tokens |
| GET  | `/api/v1/auth/me` | Current user info |

### Jobs
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/jobs` | Browse/search jobs (paginated + filtered) |
| GET | `/api/v1/jobs/{id}` | Job detail |
| GET | `/api/v1/jobs/slug/{slug}` | SEO-friendly URL |
| POST | `/api/v1/jobs` | Create job (RECRUITER) |
| PUT | `/api/v1/jobs/{id}` | Update job (RECRUITER) |
| DELETE | `/api/v1/jobs/{id}` | Soft-delete job (RECRUITER) |
| PATCH | `/api/v1/jobs/{id}/toggle` | Toggle active/inactive |
| GET | `/api/v1/jobs/my-posts` | Recruiter's own jobs |

### Applications
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/applications` | Apply to job (SEEKER) |
| GET | `/api/v1/applications/my-applications` | My applications |
| GET | `/api/v1/applications/job/{jobId}` | Applicants with skill-match scores (RECRUITER) |
| PATCH | `/api/v1/applications/{id}/status` | Update status (RECRUITER) |
| DELETE | `/api/v1/applications/{id}` | Withdraw (SEEKER) |

### Interviews
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/interviews` | Schedule interview (RECRUITER) |
| GET | `/api/v1/interviews/my-interviews` | Upcoming interviews (SEEKER) |
| GET | `/api/v1/interviews/scheduled` | Scheduled by recruiter |
| PUT | `/api/v1/interviews/{id}` | Reschedule |
| PATCH | `/api/v1/interviews/{id}/cancel` | Cancel |

### Skills & Gap Analysis
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/skills?query=java` | Autocomplete search |
| GET | `/api/v1/skills/categories` | All categories |
| GET | `/api/v1/skills/gap/{jobId}` | Skill gap analysis (SEEKER) |

---

## 🗄️ Database Schema (7 Flyway Migrations)

| Version | Description |
|---|---|
| V1 | Users, auth tables |
| V2 | Companies, seeker profiles, recruiter profiles |
| V3 | Jobs, job_skills, skills, seeker_skills, experience, education, projects |
| V4 | Applications, interviews |
| V5 | Saved jobs |
| V6 | Notifications |
| V7 | Seed 70+ skills (Java, React, Docker, etc.) |

---

## 📁 Project Structure

```
src/main/java/com/hireflow/
├── config/          # Security, Redis, JWT, Async, OpenAPI, JPA
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs
│   ├── request/
│   └── response/
├── entity/          # JPA entities
├── event/           # Spring application events
├── exception/       # GlobalExceptionHandler + custom exceptions
├── listener/        # Async event listeners
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, UserPrincipal, CustomUserDetailsService
├── service/         # Service classes
└── util/            # JaccardSimilarity, SlugUtil, ProfileCompletionCalc
```

---

*Built with ❤️ — HireFlow API*
