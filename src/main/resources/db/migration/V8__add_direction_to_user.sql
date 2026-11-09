ALTER TABLE users 
ADD COLUMN direction_id UUID REFERENCES directions(id);


-- Направление могут иметь только организаторы направления
ALTER TABLE users 
ADD CONSTRAINT chk_user_role_direction
CHECK (
    CASE 
        WHEN role = 'ORGANIZER' THEN direction_id IS NOT NULL
        ELSE direction_id IS NULL
    END
);