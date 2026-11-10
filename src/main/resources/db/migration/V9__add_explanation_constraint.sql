ALTER TABLE applications 
ADD COLUMN explanation TEXT;

-- Пояснение обязаны содержать только отправленные на доработку заявки
ALTER TABLE applications 
ADD CONSTRAINT chk_application_role_explanation
CHECK (
    CASE 
        WHEN status = 'REVISION_REQUIRED' THEN explanation IS NOT NULL
        ELSE explanation IS NULL
    END
);