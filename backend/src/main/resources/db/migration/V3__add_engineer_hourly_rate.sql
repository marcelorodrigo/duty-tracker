-- V3__add_engineer_hourly_rate.sql
-- Flyway migration: add hourly_rate column to engineer_profile table

ALTER TABLE engineer_profile
ADD COLUMN hourly_rate DECIMAL(19, 2) NOT NULL DEFAULT 1.00;
