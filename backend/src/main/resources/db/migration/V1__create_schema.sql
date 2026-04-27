-- V1__create_schema.sql
-- Flyway migration: create all tables for duty-tracker

CREATE TABLE engineer_profile (
    id              BIGSERIAL PRIMARY KEY,
    employee_type   VARCHAR(20) NOT NULL CHECK (employee_type IN ('INTERNAL','EXTERNAL')),
    working_days    VARCHAR(100) NOT NULL,
    work_start_time TIME NOT NULL,
    work_end_time   TIME NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE user_preferences (
    id              BIGSERIAL PRIMARY KEY,
    color_scheme    VARCHAR(10) NOT NULL DEFAULT 'AUTO'
                    CHECK (color_scheme IN ('DARK','LIGHT','AUTO')),
    onboarding_step VARCHAR(30) NOT NULL DEFAULT 'PROFILE'
                    CHECK (onboarding_step IN ('PROFILE','PREFERENCES','COMPENSATION_RATES','COMPLETE'))
);

CREATE TABLE compensation_rate (
    id              BIGSERIAL PRIMARY KEY,
    employee_type   VARCHAR(20) NOT NULL CHECK (employee_type IN ('INTERNAL','EXTERNAL')),
    rate_category   VARCHAR(40) NOT NULL CHECK (rate_category IN
                    ('ONCALL_WEEKDAY_SATURDAY','ONCALL_SUNDAY_HOLIDAY','OVERTIME_BASE','OVERTIME_ALLOWANCE')),
    label           VARCHAR(100) NOT NULL,
    time_from       TIME,
    time_to         TIME,
    percentage      NUMERIC(10,4) NOT NULL,
    UNIQUE (employee_type, rate_category, time_from, time_to)
);

CREATE TABLE on_call_period (
    id              BIGSERIAL PRIMARY KEY,
    start_date_time TIMESTAMP NOT NULL,
    end_date_time   TIMESTAMP NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE holiday_override (
    id                BIGSERIAL PRIMARY KEY,
    on_call_period_id BIGINT NOT NULL REFERENCES on_call_period(id) ON DELETE CASCADE,
    date              DATE NOT NULL,
    UNIQUE (on_call_period_id, date)
);

CREATE TABLE on_call_day_entry (
    id                 BIGSERIAL PRIMARY KEY,
    on_call_period_id  BIGINT NOT NULL REFERENCES on_call_period(id) ON DELETE CASCADE,
    date               DATE NOT NULL,
    hours              NUMERIC(10,4) NOT NULL,
    rate_type          VARCHAR(25) NOT NULL CHECK (rate_type IN ('WEEKDAY_SATURDAY','SUNDAY_HOLIDAY')),
    capped             BOOLEAN NOT NULL DEFAULT FALSE,
    time_for_time_flag BOOLEAN NOT NULL DEFAULT FALSE,
    manual_override    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE incident (
    id                BIGSERIAL PRIMARY KEY,
    on_call_period_id BIGINT REFERENCES on_call_period(id) ON DELETE SET NULL,
    date              DATE NOT NULL,
    start_time        TIME NOT NULL,
    end_time          TIME NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE overtime_entry (
    id                   BIGSERIAL PRIMARY KEY,
    incident_id          BIGINT NOT NULL REFERENCES incident(id) ON DELETE CASCADE,
    overtime_hours       NUMERIC(10,4) NOT NULL,
    allowance_hours      NUMERIC(10,4),
    allowance_percentage NUMERIC(10,4),
    time_from            TIME,
    time_to              TIME,
    is_allowance_entry   BOOLEAN NOT NULL DEFAULT FALSE,
    manual_override      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE registration_summary (
    id           BIGSERIAL PRIMARY KEY,
    label        VARCHAR(200) NOT NULL,
    period_start DATE NOT NULL,
    period_end   DATE NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);
