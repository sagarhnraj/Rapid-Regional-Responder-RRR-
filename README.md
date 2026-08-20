# Regional Rapid Responder (RRR) 🚨

**Regional Rapid Responder (RRR)** is an enterprise emergency-response platform built with a **Java Full-Stack Architecture** (Java 17 + Spring Boot 3 + React + Aiven PostgreSQL).

---

## 🏛️ System Architecture

```text
                  INTERNET
                     │
                     ▼
              ┌──────────────┐
              │    VERCEL    │
              │   React UI   │
              └──────┬───────┘
                     │
                 HTTPS/API & STOMP WebSockets
                     │
                     ▼
              ┌──────────────┐
              │    RENDER    │
              │ Spring Boot  │
              │ Java Backend │
              └──────┬───────┘
                     │
                 PostgreSQL (SSL)
                     │
                     ▼
              ┌──────────────┐
              │    AIVEN     │
              │ PostgreSQL   │
              └──────────────┘
```

---

## 💻 Tech Stack

- **Frontend**: React 18 + TypeScript + Vite + Tailwind CSS + Leaflet Maps (**Vercel**)
- **Backend**: Java 17 + Spring Boot 3 + Maven + Spring Security + JWT + STOMP WebSockets (**Render**)
- **Database**: PostgreSQL hosted on **Aiven** (`DATABASE_URL`)
- **Spatial Engine**: Server-Side **PostgreSQL Haversine Spatial Query** (enforces system-level 1km notification radius)

---

## 📁 Repository Structure

```text
regional-responder/
├── frontend/                     # React + TypeScript + Vite (Vercel)
│   ├── src/
│   │   ├── components/           # Reusable UI & Leaflet Map components
│   │   ├── pages/                # Citizen, Volunteer & Admin Dashboards
│   │   ├── services/             # Axios API & WebSocket Clients
│   │   ├── context/              # Auth Context
│   │   └── types/                # TypeScript DTO models
│   └── vercel.json               # Vercel SPA configuration
├── backend/                      # Java 17 + Spring Boot 3 + Maven (Render)
│   ├── pom.xml
│   ├── Dockerfile                # Multi-stage Docker container configuration
│   └── src/main/java/com/rrr/
│       ├── config/               # Security, CORS & WebSocket config
│       ├── controller/           # REST API Controllers
│       ├── dto/                  # Data Transfer Objects
│       ├── model/                # JPA Database Entities
│       ├── repository/           # Spring Data JPA Repositories
│       ├── security/             # Spring Security + JWT Filter
│       └── service/              # Core Business Logic & Spatial Engine
├── database/
│   └── schema.sql                # PostgreSQL DDL Schema & Spatial Indexes
├── docs/                         # Architecture & API documentation
└── README.md
```

---

## 🚀 Environment Variables

### Backend (`Render`)
```properties
PORT=8080
DB_URL=jdbc:postgresql://<aiven-host>:<port>/defaultdb?sslmode=require
DB_USERNAME=avnadmin
DB_PASSWORD=<aiven-password>
JWT_SECRET=<super-secret-key>
FRONTEND_URL=https://regional-responder.vercel.app
```

### Frontend (`Vercel`)
```env
VITE_API_BASE_URL=https://regional-responder-backend.onrender.com/api
VITE_WS_URL=https://regional-responder-backend.onrender.com/ws
```

---

## 🧪 Testing

### Backend
```bash
cd backend
mvn clean package
```

### Frontend
```bash
cd frontend
npx tsc --noEmit
```
