-- Remove only the untouched sample profile introduced by V2.
--
-- The id and every seeded value are matched deliberately. Profiles customized
-- through the application, or profiles recreated after deleting the sample,
-- must remain intact during an upgrade.
DELETE FROM engineer_profile
WHERE id = 1
  AND working_days = 'FRIDAY,MONDAY,THURSDAY,TUESDAY,WEDNESDAY'
  AND work_start_time = TIME '09:00'
  AND work_end_time = TIME '17:00'
  AND hourly_rate = 1.00
  AND standby_weekday_saturday_pct = 0.06700
  AND standby_sunday_holiday_pct = 0.08400;
