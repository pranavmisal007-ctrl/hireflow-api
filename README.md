# 🚀 HireFlow API

> **Production-grade Job Board & Recruitment Platform REST API**

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7-red)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue)](https://docs.docker.com/compose/)
[![Swagger](https://img.shields.io/badge/API-Swagger%20UI-85ea2d)](http://localhost:8080/swagger-ui.html)

---

## 📌 Resume Description

> Designed and built a production-grade RESTful Job Board Platform API using Spring Boot 3.2, featuring JWT-based auth with refresh token rotation, role-based access control (Seeker / Recruiter / Admin), Jaccard similarity–based skill-gap matching, async email notifications via JavaMailSender + Spring Events, Redis caching with `@Cacheable`, PDF resume parsing with Apache PDFBox (ATS scoring), paginated job search with dynamic JPA Specifications, Flyway migrations, and full Swagger/OpenAPI documentation. Containerized via Docker Compose with MySQL 8, Redis, MailHog, and MinIO.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    REST Controllers                      │
│   Auth | Jobs | Applications | Interviews | Profile      │
│   Skills | Resume | Dashboard | Notifications | Admin    │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                   Service Layer                          │
│  AuthService | JobService | ApplicationService          │
│  MatchingService | SkillGapService | ResumeParserService │
│  InterviewService | NotificationService                  │
└──────┬─────────────────────┬───────────────────────────┘
       │                     │
┌──────▼──────┐    ┌─────────▼──────┐
│ JPA Repos   │    │ Spring Events  │
│ + Flyway    │    │ + Async Exec.  │
└──────┬──────┘    └─────────┬──────┘
       │                     │
┌──────▼──────┐    ┌─────────▼──────┐
│  MySQL 8    │    │  Redis Cache   │    MinIO/S3
└─────────────┘    └────────────────┘    MailHog
```

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (JJWT) |
| Database | MySQL 8 + JPA / Hibernate |
| Migrations | Flyway |
| Caching | Redis (`@Cacheable`) |
| Email | Spring Mail + MailHog (dev) |
| File Storage | AWS S3 / MinIO |
| PDF Parsing | Apache PDFBox |
| API Docs | Swagger / SpringDoc OpenAPI |
| Testing | JUnit 5, Mockito |
| Containerization | Docker + Docker Compose |

---

## 🔐 Authentication & Roles

| Role | Capabilities |
|---|---|
| **SEEKER** | Browse jobs, apply, manage profile, skill gap analysis, upload resume |
| **RECRUITER** | Post jobs, view ranked applicants, schedule interviews, manage company |
| **ADMIN** | Platform overview, user management, force-delete jobs |

### Auth Flow
1. `POST /api/v1/auth/register` → OTP email sent
2. `POST /api/v1/auth/verify-email` → Account verified
3. `POST /api/v1/auth/login` → `{ accessToken, refreshToken }`
4. Include `Authorization: Bearer <accessToken>` on protected endpoints
5. `POST /api/v1/auth/refresh` → Rotate access token (7-day refresh token)

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

### PDF Resume ATS Scoring
`POST /api/v1/resume/analyze` (multipart/form-data):
- Extracts text with Apache PDFBox
- Detects sections (Experience, Education, Skills, Contact)
- Scores keywords, action verbs, and completeness
- Returns 0-100 ATS score with recommendations

### Async Email Notifications (Spring Events)
Events are published from services and consumed asynchronously:
- `UserRegisteredEvent` → OTP email
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

# Start all services (MySQL, Redis, MailHog, MinIO, App)
docker-compose up -d

# View logs
docker-compose logs -f app
```

The API will be available at: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html  
MailHog UI: http://localhost:8025  
MinIO Console: http://localhost:9001 (minioadmin/minioadmin)

### Run Locally

```bash
# Start infrastructure only
docker-compose up -d mysql redis mailhog minio

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
| POST | `/api/v1/auth/verify-email` | OTP verification |
| POST | `/api/v1/auth/login` | Login → JWT tokens |
| POST | `/api/v1/auth/refresh` | Rotate access token |
| POST | `/api/v1/auth/logout` | Revoke refresh token |
| POST | `/api/v1/auth/forgot-password` | Send reset link |
| POST | `/api/v1/auth/reset-password` | Reset with token |

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
| GET | `/api/v1/applications/job/{jobId}` | Ranked applicants (RECRUITER) |
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

### Resume
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/resume/upload` | Upload PDF (5MB max) |
| POST | `/api/v1/resume/analyze` | Upload + instant ATS analysis |
| GET | `/api/v1/resume` | List resumes |
| DELETE | `/api/v1/resume/{id}` | Delete resume |
| PATCH | `/api/v1/resume/{id}/set-primary` | Set primary |

---

## 🗄️ Database Schema (7 Flyway Migrations)

| Version | Description |
|---|---|
| V1 | Users, auth tokens (refresh, email-verify, password-reset) |
| V2 | Companies, seeker profiles, recruiter profiles |
| V3 | Jobs, job_skills, skills, seeker_skills, experience, education, projects |
| V4 | Resume files, applications, interviews |
| V5 | Saved jobs |
| V6 | Notifications |
| V7 | Seed 70+ skills (Java, React, AWS, Docker, etc.) |

---

## 🧪 Tests

```bash
# Run all tests
mvn test

# Tests included:
# - JaccardSimilarityUtilTest (7 test cases)
# - SlugUtilTest (7 test cases)
# - JwtUtilTest (4 test cases)
# - SkillGapServiceTest (3 test cases)
# - HireFlowApplicationTest (context load)
```

---

## 📁 Project Structure

```
src/main/java/com/hireflow/
├── config/          # Security, Redis, S3, JWT, Async, OpenAPI, JPA
├── controller/      # 10 REST controllers
├── dto/             # Request/Response DTOs
│   ├── request/
│   └── response/
├── entity/          # 24 JPA entities
├── event/           # 4 Spring application events
├── exception/       # GlobalExceptionHandler + custom exceptions
├── listener/        # 4 async event listeners
├── repository/      # 19 Spring Data JPA repositories
├── security/        # JWT filter, UserPrincipal, CustomUserDetailsService
├── service/         # 14 service classes
└── util/            # JaccardSimilarity, SlugUtil, ProfileCompletionCalc
```

---

## 🏷️ Commit Checkpoints

| Checkpoint | Description |
|---|---|
| `CHECKPOINT-1` | Project setup, pom.xml, configuration, security layer |
| `CHECKPOINT-2` | All 24 entities + 19 repositories |
| `CHECKPOINT-3` | Exceptions, DTOs, utilities, Spring Events |
| `CHECKPOINT-4` | All 14 services (Auth, Job, Application, Interview, etc.) |
| `CHECKPOINT-5` | All 10 controllers, Flyway migrations, Docker, README |

---

*Built with ❤️ — HireFlow API*
