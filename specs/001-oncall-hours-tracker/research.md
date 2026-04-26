# Research: On-Call Hours Tracker

**Phase**: 0 | **Date**: 2026-04-25 (updated) | **Plan**: [plan.md](plan.md)

---

## 1. Spring Boot 4 + Java 25

**Decision**: Spring Boot 4.x (latest stable) on Java 25.

**Rationale**: Spring Boot 4 is built on Spring Framework 7 and requires Java 25+. It brings virtual threads on by default (`spring.threads.virtual.enabled=true` is the default), improved observability, and Jakarta EE 11. Java 25 provides finalized virtual threads, records, sealed classes, and pattern matching — all used in this project for domain records and switch expressions in calculation logic.

**Key notes**:
- Package prefix: `jakarta.*` (not `javax.*`) everywhere — Jakarta EE 11.
- Constructor injection is the only permitted DI style. No `@Autowired` on fields anywhere.
- Spring Data JDBC remains the persistence approach — no JPA/Hibernate.
- Spring Boot auto-configures Flyway when `flyway-core` is on the classpath.
- Virtual threads: default in Spring Boot 4; no extra config needed. Benefits Tomcat threads during any I/O wait.

**Bootstrap**: Spring Initializr selections:
- Project: Maven | Language: Java | Spring Boot: 4.x (latest)
- Group: `com.dutytracker` | Artifact: `duty-tracker-backend` | Java: 25
- Dependencies: Spring Web, Spring Data JDBC, Flyway Migration, Validation, PostgreSQL Driver

---

## 2. Spring Data JDBC + PostgreSQL 18

**Decision**: Spring Data JDBC with `org.postgresql:postgresql` JDBC driver against PostgreSQL 18.

**Rationale**: Spring Data JDBC gives explicit, predictable SQL without JPA's session and lazy-loading complexity. PostgreSQL 18 (released September 2025, latest patch 18.3) supports native `DATE`, `TIME`, `TIMESTAMP`, `NUMERIC`, `BOOLEAN`, and enum types — no custom type converters needed (unlike the previous SQLite approach). PostgreSQL runs as a Docker container so no host installation is required.

**DataSource config** (`application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:dutytracker}
    username: ${DB_USER:dutytracker}
    password: ${DB_PASSWORD:dutytracker}
  data:
    jdbc:
      dialect: postgresql
```

**Key mapping notes**:
- `LocalDate` → `DATE` (native, no converter)
- `LocalTime` → `TIME` (native)
- `LocalDateTime` → `TIMESTAMP` (native)
- `BigDecimal` → `NUMERIC(10,4)` (native)
- `Boolean` → `BOOLEAN` (native)
- Java enums → `VARCHAR` with `CHECK` constraint (stored as enum name string)
- `Set<DayOfWeek>` → `VARCHAR` column, comma-separated (custom converter still needed for this field only)

**Alternatives considered**:
- JPA/Hibernate: Rejected. Adds session management, lazy-loading pitfalls, and unnecessary complexity for a simple schema.
- H2 file mode: Rejected. Not PostgreSQL — loses production parity and type compatibility.
- SQLite (previous plan): Rejected. Required custom converters for every type and has limited tooling support.

---

## 3. Flyway Database Migrations

**Decision**: Flyway (`org.flywaydb:flyway-core` + `flyway-database-postgresql`) for schema versioning.

**Rationale**: Flyway tracks schema changes as versioned SQL scripts, enabling reproducible setup from scratch and safe incremental updates. Spring Boot auto-configures Flyway when it detects the dependency — no code needed beyond placing migration files in `src/main/resources/db/migration/`.

**Migration file naming convention**:
```
V1__create_schema.sql          ← all tables
V2__seed_compensation_rates.sql ← default WCA rate rows
```

**Config** (`application.yml`):
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
```

**Test strategy**: Use `@FlywayTest` (or Spring Boot Test with an embedded PostgreSQL via `testcontainers`) to run migrations against a real database in integration tests.

**Alternatives considered**:
- `spring.sql.init.mode=always`: No versioning, no migration history table, not appropriate beyond initial setup.
- Liquibase: Valid alternative but more verbose XML/YAML format. Flyway SQL-first approach is simpler for this project.

---

## 4. Dutch Public Holiday Calendar

**Decision**: `de.focus-shift:jollyday-core` (actively maintained fork of Jollyday).

**Rationale**: Supports the Dutch public holiday calendar (`HolidayCalendar.NETHERLANDS`) including variable-date holidays (Easter Monday, Ascension, Whit Monday). No network access at runtime.

**Maven coordinates**:
```xml
<dependency>
    <groupId>de.focus-shift</groupId>
    <artifactId>jollyday-core</artifactId>
    <version>0.26.0</version>
</dependency>
```

**Integration**: Implement `PublicHolidayGateway` in `infrastructure/holiday/JollydayPublicHolidayGateway`. Domain never imports Jollyday.

**Alternatives considered**: Hardcoded list (brittle), Nager.Date API (requires internet).

---

## 5. Nuxt 4 + pnpm

**Decision**: Nuxt 4 (latest), bootstrapped with `pnpm create nuxt@latest`. Package manager: pnpm throughout.

**Rationale**: Nuxt 4 is the current major version with improved routing, composable-first architecture, and better TypeScript support. pnpm is faster, more disk-efficient, and enforces strict dependency isolation compared to npm.

**Bootstrap**:
```bash
pnpm create nuxt@latest frontend
cd frontend
pnpm install
```

**Nuxt modules** (`nuxt.config.ts`):
```ts
modules: [
  '@nuxt/ui',       // UI components, Tailwind v4, icons, color-mode, fonts — all bundled
  '@pinia/nuxt',    // state management
  '@vueuse/nuxt',   // composable utilities (useDateFormat, useLocalStorage, etc.)
  'dayjs-nuxt',     // date formatting with Dutch locale (nl)
]
```

**Install**:
```bash
pnpm add @nuxt/ui @pinia/nuxt @vueuse/nuxt dayjs-nuxt
```

**`nuxt.config.ts`**:
```ts
export default defineNuxtConfig({
  modules: ['@nuxt/ui', '@pinia/nuxt', '@vueuse/nuxt', 'dayjs-nuxt'],
  colorMode: {
    preference: 'system',
    fallback: 'light',
  },
  dayjs: {
    locales: ['nl'],
    defaultLocale: 'nl',
  },
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE ?? 'http://localhost:8080/api/v1',
    },
  },
})
```

**`assets/css/main.css`** (Tailwind v4 — CSS-first, no `tailwind.config.ts`):
```css
@import "tailwindcss";
@import "@nuxt/ui";
```

**pnpm config** (`.npmrc` at repo root) — `shamefully-hoist=true` is required for Nuxt UI v3 with pnpm:
```
shamefully-hoist=true
strict-peer-dependencies=false
```

**Alternatives considered**: npm (slower, less strict); yarn (less standard in Vue/Nuxt ecosystem now).

---

## 6. Color Scheme — `@nuxt/ui` built-in color mode

**Decision**: Use `@nuxt/ui` v3's bundled `@nuxtjs/color-mode` integration. No separate module entry needed.

**Rationale**: Nuxt UI v3 automatically registers `@nuxtjs/color-mode` (and `@nuxt/icon` and `@nuxt/fonts`) when `@nuxt/ui` is listed in `modules`. Color mode is configured via the `colorMode` key in `nuxt.config.ts`. The `UColorModeSwitch` / `UColorModeButton` components are available out of the box — no custom toggle needed.

**Config** (`nuxt.config.ts`):
```ts
colorMode: {
  preference: 'system',
  fallback: 'light',
}
```

Tailwind CSS v4 dark mode is handled automatically by Nuxt UI — no `darkMode: 'class'` or `tailwind.config.ts` entry required.

---

## 7. Docker + Docker Compose

**Decision**: Each service (backend, frontend, postgres) has its own `Dockerfile`. A root-level `docker-compose.yml` orchestrates all three.

**Backend Dockerfile** (`backend/Dockerfile`):
```dockerfile
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/duty-tracker-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend Dockerfile** (`frontend/Dockerfile`):
```dockerfile
FROM node:24-alpine AS build
RUN npm install -g pnpm
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN pnpm build

FROM nginx:alpine
COPY --from=build /app/.output/public /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**`docker-compose.yml`** (repo root):
```yaml
services:
  postgres:
    image: postgres:18-alpine
    environment:
      POSTGRES_DB: dutytracker
      POSTGRES_USER: dutytracker
      POSTGRES_PASSWORD: dutytracker
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U dutytracker"]
      interval: 5s
      retries: 5

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: dutytracker
      DB_USER: dutytracker
      DB_PASSWORD: dutytracker
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    environment:
      NUXT_PUBLIC_API_BASE: http://localhost:8080/api/v1
    depends_on:
      - backend

volumes:
  postgres_data:
```

**Alternatives considered**: Host-installed PostgreSQL (environment-dependent, harder to reset); Kubernetes (overkill for local single-user tool).

---

## 8. Frontend–Backend Communication

**Decision**: Nuxt 4 `$fetch` / `useFetch` composables calling the Spring Boot REST API at `http://localhost:8080/api/v1`.

**CORS**: Spring Boot allows `http://localhost:3000` via a global `WebMvcConfigurer` CORS bean in `infrastructure/config/CorsConfiguration.java`.

**Error handling**: Map Spring Boot `ProblemDetail` (RFC 7807) error responses to user-facing messages via a shared `useApiError` composable.

**API base URL**: `runtimeConfig.public.apiBase` in `nuxt.config.ts`, defaulting to `http://localhost:8080/api/v1`.

---

## 9. Timezone Handling

**Decision**: Store all date/time values as local time (no timezone offset). Assume Dutch local time (Europe/Amsterdam) throughout.

**Rationale**: Personal local tool; the engineer's device and Docker host clock are in Dutch time. All policy rules operate on local calendar time. PostgreSQL `TIMESTAMP WITHOUT TIME ZONE` is used — correct for this use case.

**Overnight boundary**: When an on-call period or incident crosses midnight, the calculation logic splits hours at the calendar day boundary by date arithmetic on `LocalDateTime`. No timezone conversion required.

---

## 10. WCA Compensation Rate Table — Seed Data

**Decision**: Pre-load default rows via Flyway migration `V2__seed_compensation_rates.sql`.

**⚠️ Action required before go-live**: The WCA allowance percentage table is an image in the Confluence page and the exact values could not be extracted automatically. **Extract the actual percentages from the WCA PDF document and update `V2__seed_compensation_rates.sql` before first deployment.**

**Placeholder approach**: Migration inserts rows with `0.00` percentage and a comment. The onboarding wizard Compensation Rate Table step prompts the engineer to review and adjust before first use.
