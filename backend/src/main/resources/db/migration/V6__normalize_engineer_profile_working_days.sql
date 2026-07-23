CREATE TABLE engineer_profile_working_day (
    engineer_profile_id BIGINT NOT NULL,
    working_day         VARCHAR(9) NOT NULL,
    CONSTRAINT pk_engineer_profile_working_day
        PRIMARY KEY (engineer_profile_id, working_day),
    CONSTRAINT fk_engineer_profile_working_day_profile
        FOREIGN KEY (engineer_profile_id) REFERENCES engineer_profile(id) ON DELETE CASCADE,
    CONSTRAINT ck_engineer_profile_working_day
        CHECK (working_day IN (
            'MONDAY',
            'TUESDAY',
            'WEDNESDAY',
            'THURSDAY',
            'FRIDAY',
            'SATURDAY',
            'SUNDAY'
        ))
);

INSERT INTO engineer_profile_working_day (engineer_profile_id, working_day)
SELECT DISTINCT profile.id, BTRIM(days.working_day)
FROM engineer_profile profile
CROSS JOIN LATERAL UNNEST(STRING_TO_ARRAY(profile.working_days, ',')) AS days(working_day)
WHERE BTRIM(days.working_day) <> '';

CREATE INDEX idx_engineer_profile_working_day_day
    ON engineer_profile_working_day (working_day, engineer_profile_id);

ALTER TABLE engineer_profile
    DROP COLUMN working_days;
