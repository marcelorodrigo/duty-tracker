-- Profile business defaults are owned by app.profile-defaults configuration.
-- Keep the columns required so every persistence path must provide explicit values.
ALTER TABLE engineer_profile
    ALTER COLUMN hourly_rate DROP DEFAULT,
    ALTER COLUMN standby_weekday_saturday_pct DROP DEFAULT,
    ALTER COLUMN standby_sunday_holiday_pct DROP DEFAULT;
