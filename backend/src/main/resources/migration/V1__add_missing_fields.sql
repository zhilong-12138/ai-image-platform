-- V1__add_missing_fields.sql
-- Add missing fields for invite system and prompt configuration

-- sys_user: add invitee_id to track which user referred this user
ALTER TABLE sys_user ADD COLUMN invitee_id BIGINT;

-- prompt: add cost_points and ref_image_count
ALTER TABLE prompt ADD COLUMN cost_points INT DEFAULT 10;
ALTER TABLE prompt ADD COLUMN ref_image_count INT DEFAULT 0;

-- point_log: add related_id to link to generation_record.id
ALTER TABLE point_log ADD COLUMN related_id BIGINT;
