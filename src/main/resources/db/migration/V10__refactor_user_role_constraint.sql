-- Удалим старое ограничение
ALTER TABLE users DROP CONSTRAINT chk_user_role_direction;

-- Добавим новое
ALTER TABLE users 
ADD CONSTRAINT chk_user_role_direction
CHECK (
    CASE 
        WHEN role = 'ORGANIZER' THEN direction_id IS NOT NULL
        WHEN role = 'EXPERT' THEN direction_id IS NOT NULL
        ELSE direction_id IS NULL
    END
);