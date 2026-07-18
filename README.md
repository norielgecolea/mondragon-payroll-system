# Mondragon Food Products — Payroll System v3

Single-admin payroll system built with **Spring Boot + Hibernate**, **Angular**, **PostgreSQL**, **nginx**, and **Docker**.

## Features

- Admin login (JWT)
- Manage employees, positions, salary rates
- Employee schedules
- Encode DTR (daily time record)
- Encode overtime (**only approved OT is paid**)
- Cash advances with optional payroll deduction
- In-company savings accounts (deposit / withdraw / payroll deduction)
- Generate, finalize, print, and archive payroll

## Stack layout

```
payroll-system-v3/
├── mondragon-payroll-system/   # Spring Boot API (controller → service → repo → model/dto)
├── frontend/payroll-ui/        # Angular admin UI
├── nginx/                      # Reverse proxy configs
├── docker-compose.yml          # Production stack
└── docker-compose.dev.yml      # Dev stack with live Angular
```

## Default admin

- Username: `admin`
- Password: `admin123`

## Quick start (Docker)

```bash
docker compose up --build
```

Open http://localhost

API: http://localhost/api  
Direct backend: http://localhost:8080/api

## Local development

1. Start Postgres (or `docker compose -f docker-compose.dev.yml up postgres -d`)
2. Run backend:

```bash
cd mondragon-payroll-system
mvn spring-boot:run
```

3. Run frontend:

```bash
cd frontend/payroll-ui
npm install
npx ng serve --proxy-config proxy.conf.js
```

Open http://localhost:4200

## Payroll rules

- Basic pay = days with DTR × daily rate
- Overtime pay = approved OT hours × overtime rate only
- Cash advance / savings deductions are optional at generation time
- Finalizing payroll applies CA balance reduction and credits savings
- Archiving stores a JSON snapshot for reprint/audit
