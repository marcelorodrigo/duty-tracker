-- V2__seed_data.sql
-- Seeds all reference data:
--   1. Default engineer profile (Mon–Fri, 09:00–17:00)
--   2. Base compensation rates (on-call + overtime base) for INTERNAL
--   3. WCA overtime allowance rates (72 rows: 24 hourly slots × 3 day types)
--      Source: Jumbo Logistics WCA P7-2025, 24/7 working hours scheme
--
-- NOTE: On-call percentages are initialised to 0 — update them via Settings once confirmed.

-- ── 1. Default engineer profile ───────────────────────────────────────────────
-- working_days is stored as a comma-separated sorted list of DayOfWeek names.
INSERT INTO engineer_profile (working_days, work_start_time, work_end_time)
VALUES ('FRIDAY,MONDAY,THURSDAY,TUESDAY,WEDNESDAY', '09:00', '17:00');

-- ── 2. Base compensation rates ────────────────────────────────────────────────
INSERT INTO compensation_rate (rate_category, label, time_from, time_to, percentage) VALUES
    ('ONCALL_WEEKDAY_SATURDAY', 'On-call Monday–Saturday',  NULL, NULL,   0.0000),
    ('ONCALL_SUNDAY_HOLIDAY',   'On-call Sunday / Holiday', NULL, NULL,   0.0000),
    ('OVERTIME_BASE',           'Overtime base rate',       NULL, NULL, 100.0000);

-- ── 3. WCA overtime allowance rates ──────────────────────────────────────────
--  WEEKDAY (Mon–Fri): 00:00–06:00 = 50%, 06:00–18:00 = 0%, 18:00–22:00 = 35%, 22:00–00:00 = 50%
--  SATURDAY (non-basic obligatory): 00:00–22:00 = 50%, 22:00–00:00 = 75%
--  SUNDAY/HOLIDAY: all 24 hours = 100%
--
-- To update a rate: UPDATE compensation_rate SET percentage = <new_value>
--   WHERE overtime_day_type = '<day_type>'
--   AND time_from = '<HH:MM>' AND time_to = '<HH:MM>';

INSERT INTO compensation_rate (rate_category, overtime_day_type, label, time_from, time_to, percentage) VALUES

-- ── WEEKDAY ───────────────────────────────────────────────────────────────────
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 00:00-01:00', '00:00', '01:00', 50.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 01:00-02:00', '01:00', '02:00', 50.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 02:00-03:00', '02:00', '03:00', 50.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 03:00-04:00', '03:00', '04:00', 50.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 04:00-05:00', '04:00', '05:00', 50.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 05:00-06:00', '05:00', '06:00', 50.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 06:00-07:00', '06:00', '07:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 07:00-08:00', '07:00', '08:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 08:00-09:00', '08:00', '09:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 09:00-10:00', '09:00', '10:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 10:00-11:00', '10:00', '11:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 11:00-12:00', '11:00', '12:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 12:00-13:00', '12:00', '13:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 13:00-14:00', '13:00', '14:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 14:00-15:00', '14:00', '15:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 15:00-16:00', '15:00', '16:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 16:00-17:00', '16:00', '17:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 17:00-18:00', '17:00', '18:00',  0.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 18:00-19:00', '18:00', '19:00', 35.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 19:00-20:00', '19:00', '20:00', 35.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 20:00-21:00', '20:00', '21:00', 35.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 21:00-22:00', '21:00', '22:00', 35.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 22:00-23:00', '22:00', '23:00', 50.0000),
('OVERTIME_ALLOWANCE', 'WEEKDAY', 'Weekday 23:00-00:00', '23:00', '00:00', 50.0000),

-- ── SATURDAY (non-basic obligatory) ──────────────────────────────────────────
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 00:00-01:00', '00:00', '01:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 01:00-02:00', '01:00', '02:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 02:00-03:00', '02:00', '03:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 03:00-04:00', '03:00', '04:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 04:00-05:00', '04:00', '05:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 05:00-06:00', '05:00', '06:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 06:00-07:00', '06:00', '07:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 07:00-08:00', '07:00', '08:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 08:00-09:00', '08:00', '09:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 09:00-10:00', '09:00', '10:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 10:00-11:00', '10:00', '11:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 11:00-12:00', '11:00', '12:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 12:00-13:00', '12:00', '13:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 13:00-14:00', '13:00', '14:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 14:00-15:00', '14:00', '15:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 15:00-16:00', '15:00', '16:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 16:00-17:00', '16:00', '17:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 17:00-18:00', '17:00', '18:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 18:00-19:00', '18:00', '19:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 19:00-20:00', '19:00', '20:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 20:00-21:00', '20:00', '21:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 21:00-22:00', '21:00', '22:00', 50.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 22:00-23:00', '22:00', '23:00', 75.0000),
('OVERTIME_ALLOWANCE', 'SATURDAY', 'Saturday 23:00-00:00', '23:00', '00:00', 75.0000),

-- ── SUNDAY/HOLIDAY ────────────────────────────────────────────────────────────
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 00:00-01:00', '00:00', '01:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 01:00-02:00', '01:00', '02:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 02:00-03:00', '02:00', '03:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 03:00-04:00', '03:00', '04:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 04:00-05:00', '04:00', '05:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 05:00-06:00', '05:00', '06:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 06:00-07:00', '06:00', '07:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 07:00-08:00', '07:00', '08:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 08:00-09:00', '08:00', '09:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 09:00-10:00', '09:00', '10:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 10:00-11:00', '10:00', '11:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 11:00-12:00', '11:00', '12:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 12:00-13:00', '12:00', '13:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 13:00-14:00', '13:00', '14:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 14:00-15:00', '14:00', '15:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 15:00-16:00', '15:00', '16:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 16:00-17:00', '16:00', '17:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 17:00-18:00', '17:00', '18:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 18:00-19:00', '18:00', '19:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 19:00-20:00', '19:00', '20:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 20:00-21:00', '20:00', '21:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 21:00-22:00', '21:00', '22:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 22:00-23:00', '22:00', '23:00', 100.0000),
('OVERTIME_ALLOWANCE', 'SUNDAY_HOLIDAY', 'Sunday/Holiday 23:00-00:00', '23:00', '00:00', 100.0000);
