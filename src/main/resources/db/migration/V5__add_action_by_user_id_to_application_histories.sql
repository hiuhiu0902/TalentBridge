ALTER TABLE application_histories
    ADD COLUMN action_by_user_id BIGINT NULL,
ADD CONSTRAINT fk_application_histories_action_by_user
FOREIGN KEY (action_by_user_id) REFERENCES users(id) ON DELETE SET NULL;