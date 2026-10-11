CREATE TABLE IF NOT EXISTS conference_settings (
    id SMALLINT PRIMARY KEY CHECK (id = 1), -- В таблице пока что не больше одной записи
    submission_deadline TIMESTAMPTZ NOT NULL,
    moderation_deadline TIMESTAMPTZ NOT NULL,
    CONSTRAINT check_conference_deadlines
        CHECK (moderation_deadline >= submission_deadline)
);