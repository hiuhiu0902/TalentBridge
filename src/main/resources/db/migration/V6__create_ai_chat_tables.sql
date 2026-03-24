CREATE TABLE ai_chat_sessions (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  user_id BIGINT NOT NULL,
                                  title VARCHAR(255) NULL,
                                  role_snapshot VARCHAR(20) NOT NULL,
                                  active BIT(1) NOT NULL,
                                  last_message_at DATETIME(6) NULL,
                                  created_at DATETIME(6) NOT NULL,
                                  updated_at DATETIME(6) NOT NULL,
                                  PRIMARY KEY (id),
                                  KEY idx_ai_chat_sessions_user_id (user_id),
                                  KEY idx_ai_chat_sessions_last_message_at (last_message_at),
                                  CONSTRAINT fk_ai_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_chat_messages (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  session_id BIGINT NOT NULL,
                                  sender_type VARCHAR(20) NOT NULL,
                                  content LONGTEXT NOT NULL,
                                  model_name VARCHAR(100) NULL,
                                  used_tools VARCHAR(500) NULL,
                                  created_at DATETIME(6) NOT NULL,
                                  PRIMARY KEY (id),
                                  KEY idx_ai_chat_messages_session_id (session_id),
                                  KEY idx_ai_chat_messages_session_created_at (session_id, created_at),
                                  CONSTRAINT fk_ai_chat_messages_session FOREIGN KEY (session_id) REFERENCES ai_chat_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
