CREATE TABLE IF NOT EXISTS participant_profile (
    user_id         UUID PRIMARY KEY REFERENCES users(id),
    first_name       VARCHAR(255) NOT NULL,
    surname         VARCHAR(255) NOT NULL,
    patronymic      VARCHAR(255),
    group_name      VARCHAR(50) NOT NULL,
    phone           VARCHAR(255) NOT NULL,
    funding_type    VARCHAR(50) NOT NULL           -- BUDGET, CONTRACT
);


ALTER TABLE users
RENAME COLUMN fullname TO alias;