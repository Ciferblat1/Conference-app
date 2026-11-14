ALTER TABLE applications 
ADD COLUMN pending_direction_id UUID REFERENCES directions(id);