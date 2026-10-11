CREATE TABLE IF NOT EXISTS conference_settings (
    id SMALLINT PRIMARY KEY CHECK (id = 1), -- В таблице пока что не больше одной записи
    submission_deadline TIMESTAMPTZ NOT NULL,
    moderation_deadline TIMESTAMPTZ NOT NULL,
    CONSTRAINT check_conference_deadlines
        CHECK (moderation_deadline >= submission_deadline)
);


INSERT INTO conference_settings (
    id,
    submission_deadline,
    moderation_deadline
)
VALUES (
    1,
    TIMESTAMPTZ '2026-11-30 18:00:00+03:00',
    TIMESTAMPTZ '2026-12-30 18:00:00+03:00'
);