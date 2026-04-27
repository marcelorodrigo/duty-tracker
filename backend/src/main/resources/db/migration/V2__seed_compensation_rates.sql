-- V2__seed_compensation_rates.sql
-- WCA PLACEHOLDER: percentages below must be updated from the WCA PDF
-- (Jumbo Logistics WCA, version P7-2025) before production use.
-- The engineer is prompted to review these values during onboarding Step 3 and in Settings.

INSERT INTO compensation_rate (employee_type, rate_category, label, time_from, time_to, percentage) VALUES
    ('INTERNAL', 'ONCALL_WEEKDAY_SATURDAY', 'On-call Monday–Saturday',  NULL, NULL, 0.0000),
    ('INTERNAL', 'ONCALL_SUNDAY_HOLIDAY',   'On-call Sunday / Holiday', NULL, NULL, 0.0000),
    ('INTERNAL', 'OVERTIME_BASE',           'Overtime base rate',       NULL, NULL, 100.0000),

    ('EXTERNAL', 'ONCALL_WEEKDAY_SATURDAY', 'On-call Monday–Saturday',  NULL, NULL, 0.0000),
    ('EXTERNAL', 'ONCALL_SUNDAY_HOLIDAY',   'On-call Sunday / Holiday', NULL, NULL, 0.0000),
    ('EXTERNAL', 'OVERTIME_BASE',           'Overtime base rate',       NULL, NULL, 100.0000);
