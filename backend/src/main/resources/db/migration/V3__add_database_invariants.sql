-- Prevent concurrent writes from violating scheduling invariants that are also
-- checked by the application validators.

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE UNIQUE INDEX uq_engineer_profile_singleton
    ON engineer_profile ((TRUE));

ALTER TABLE on_call_period
    ADD CONSTRAINT ex_on_call_period_no_overlap
    EXCLUDE USING gist (
        tsrange(start_date_time, end_date_time, '[)') WITH &&
    );

ALTER TABLE incident
    ADD CONSTRAINT ex_incident_no_overlap
    EXCLUDE USING gist (
        on_call_period_id WITH =,
        tsrange(start_date_time, end_date_time, '[)') WITH &&
    );
