DROP TABLE application_documents CASCADE;

DELETE FROM applications;

ALTER TABLE applications 
    ADD COLUMN title                VARCHAR(255) NOT NULL,
    ADD COLUMN theses               TEXT NOT NULL,
    ADD COLUMN author_first_name    VARCHAR(255) NOT NULL,
    ADD COLUMN author_surname       VARCHAR(255) NOT NULL,
    ADD COLUMN author_patronymic    VARCHAR(255),
    ADD COLUMN author_phone         VARCHAR(255) NOT NULL,
    ADD COLUMN author_group_name    VARCHAR(50) NOT NULL,
    ADD COLUMN author_email         VARCHAR(255) NOT NULL,
    ADD COLUMN author_funding_type  VARCHAR(50) NOT NULL;


CREATE TABLE IF NOT EXISTS application_references_list (

    application_id UUID NOT NULL REFERENCES applications(id),
    references_order INTEGER NOT NULL,
    reference_text TEXT NOT NULL,
    PRIMARY KEY (application_id, references_order),
    CHECK (references_order >= 0)
);

CREATE TABLE IF NOT EXISTS application_supervisors (

    application_id UUID NOT NULL REFERENCES applications(id),
    supervisors_order       INTEGER NOT NULL,
    supervisor_first_name   VARCHAR(255) NOT NULL,
    supervisor_surname      VARCHAR(255) NOT NULL,
    supervisor_patronymic   VARCHAR(255),
    supervisor_position     VARCHAR(255) NOT NULL,
    supervisor_department   VARCHAR(255) NOT NULL,
    PRIMARY KEY (application_id, supervisors_order),
    CHECK (supervisors_order >= 0)
);