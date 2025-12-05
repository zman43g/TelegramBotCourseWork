-- liquibase formatted sql

-- changeset sfilimonov:1
-- comment: Create notification_task table
CREATE TABLE IF NOT EXISTS notification_task (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    notification_date_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP
);

