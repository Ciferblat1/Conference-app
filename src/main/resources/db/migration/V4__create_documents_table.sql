CREATE TABLE IF NOT EXISTS application_documents (
    application_id UUID PRIMARY KEY REFERENCES applications(id),
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    content BYTEA NOT NULL,
    CONSTRAINT check_application_document_size
        CHECK (octet_length(content) BETWEEN 1 AND 10485760)
);

ALTER TABLE applications
DROP COLUMN body;