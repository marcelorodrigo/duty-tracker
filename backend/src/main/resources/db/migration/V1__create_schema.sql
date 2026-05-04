-- V1__create_schema.sql
-- Flyway migration: create all tables for duty-tracker

CREATE TABLE engineer_profile (
    id              BIGSERIAL PRIMARY KEY,
    working_days    VARCHAR(100) NOT NULL,
    work_start_time TIME NOT NULL,
    work_end_time   TIME NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE compensation_rate (
    id                BIGSERIAL PRIMARY KEY,
    rate_category     VARCHAR(40)  NOT NULL CHECK (rate_category IN
                      ('ONCALL_WEEKDAY_SATURDAY','ONCALL_SUNDAY_HOLIDAY','OVERTIME_BASE','OVERTIME_ALLOWANCE')),
    overtime_day_type VARCHAR(20)  CHECK (overtime_day_type IN ('WEEKDAY', 'SATURDAY', 'SUNDAY_HOLIDAY')),
    label             VARCHAR(100) NOT NULL,
    time_from         TIME,
    time_to           TIME,
    percentage        NUMERIC(10,4) NOT NULL,
    CONSTRAINT uq_compensation_rate
        UNIQUE NULLS NOT DISTINCT (rate_category, overtime_day_type, time_from, time_to)
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

CREATE TABLE incident (
    id                BIGSERIAL PRIMARY KEY,
    on_call_period_id BIGINT NOT NULL REFERENCES on_call_period(id) ON DELETE RESTRICT,
    name              VARCHAR(255) NOT NULL,
    start_date_time   TIMESTAMP NOT NULL,
    end_date_time     TIMESTAMP NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);
