CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL,
                       full_name VARCHAR(100) NULL,
                       password VARCHAR(255) NULL,
                       role VARCHAR(20) NOT NULL,
                       active BIT(1) NOT NULL,
                       avatar_url VARCHAR(255) NULL,
                       provider VARCHAR(50) NULL,
                       provider_id VARCHAR(255) NULL,
                       created_at DATETIME(6) NULL,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_username (username),
                       UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categories (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL,
                            description TEXT NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE employers (
                           user_id BIGINT NOT NULL,
                           company_name VARCHAR(200) NOT NULL,
                           website VARCHAR(255) NULL,
                           description TEXT NULL,
                           logo_url VARCHAR(255) NULL,
                           industry VARCHAR(100) NULL,
                           company_size VARCHAR(100) NULL,
                           address VARCHAR(255) NULL,
                           PRIMARY KEY (user_id),
                           CONSTRAINT fk_employers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE candidates (
                            user_id BIGINT NOT NULL,
                            phone VARCHAR(20) NULL,
                            address VARCHAR(255) NULL,
                            summary TEXT NULL,
                            cv_url VARCHAR(255) NULL,
                            avatar_url VARCHAR(255) NULL,
                            PRIMARY KEY (user_id),
                            CONSTRAINT fk_candidates_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               content TEXT NULL,
                               type VARCHAR(50) NULL,
                               reference_url VARCHAR(255) NULL,
                               is_read BIT(1) NOT NULL,
                               created_at DATETIME(6) NULL,
                               PRIMARY KEY (id),
                               KEY idx_notifications_user_id (user_id),
                               KEY idx_notifications_user_read (user_id, is_read),
                               CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE job_posts (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           employer_id BIGINT NOT NULL,
                           category_id BIGINT NULL,
                           title VARCHAR(200) NOT NULL,
                           description TEXT NULL,
                           salary_min DECIMAL(38,2) NULL,
                           salary_max DECIMAL(38,2) NULL,
                           location VARCHAR(100) NULL,
                           job_type VARCHAR(50) NULL,
                           experience_level VARCHAR(50) NULL,
                           status VARCHAR(30) NOT NULL,
                           posted_at DATETIME(6) NULL,
                           expired_at DATETIME(6) NULL,
                           rejection_reason TEXT NULL,
                           PRIMARY KEY (id),
                           KEY idx_job_posts_employer_id (employer_id),
                           KEY idx_job_posts_category_id (category_id),
                           KEY idx_job_posts_status (status),
                           KEY idx_job_posts_posted_at (posted_at),
                           CONSTRAINT fk_job_posts_employer FOREIGN KEY (employer_id) REFERENCES employers(user_id) ON DELETE CASCADE,
                           CONSTRAINT fk_job_posts_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE job_skills (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            job_post_id BIGINT NOT NULL,
                            skill_name VARCHAR(100) NOT NULL,
                            level VARCHAR(30) NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_job_skills_job_post_skill_name (job_post_id, skill_name),
                            KEY idx_job_skills_skill_name (skill_name),
                            CONSTRAINT fk_job_skills_job_post FOREIGN KEY (job_post_id) REFERENCES job_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE applications (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              candidate_id BIGINT NOT NULL,
                              job_post_id BIGINT NOT NULL,
                              status VARCHAR(30) NOT NULL,
                              cv_url_at_time VARCHAR(255) NULL,
                              cover_letter TEXT NULL,
                              applied_at DATETIME(6) NULL,
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_applications_candidate_job_post (candidate_id, job_post_id),
                              KEY idx_applications_candidate_id (candidate_id),
                              KEY idx_applications_job_post_id (job_post_id),
                              KEY idx_applications_status (status),
                              CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE,
                              CONSTRAINT fk_applications_job_post FOREIGN KEY (job_post_id) REFERENCES job_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE application_histories (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       application_id BIGINT NOT NULL,
                                       from_status VARCHAR(30) NULL,
                                       to_status VARCHAR(30) NOT NULL,
                                       note TEXT NULL,
                                       changed_at DATETIME(6) NULL,
                                       PRIMARY KEY (id),
                                       KEY idx_application_histories_application_id (application_id),
                                       CONSTRAINT fk_application_histories_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE interviews (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            application_id BIGINT NOT NULL,
                            interview_at DATETIME(6) NULL,
                            location VARCHAR(255) NULL,
                            meeting_link VARCHAR(255) NULL,
                            note TEXT NULL,
                            status VARCHAR(30) NULL,
                            created_at DATETIME(6) NULL,
                            PRIMARY KEY (id),
                            KEY idx_interviews_application_id (application_id),
                            KEY idx_interviews_interview_at (interview_at),
                            CONSTRAINT fk_interviews_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE saved_jobs (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            candidate_id BIGINT NOT NULL,
                            job_post_id BIGINT NOT NULL,
                            saved_at DATETIME(6) NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_saved_jobs_candidate_job_post (candidate_id, job_post_id),
                            KEY idx_saved_jobs_candidate_id (candidate_id),
                            KEY idx_saved_jobs_job_post_id (job_post_id),
                            CONSTRAINT fk_saved_jobs_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE,
                            CONSTRAINT fk_saved_jobs_job_post FOREIGN KEY (job_post_id) REFERENCES job_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE follow_connections (
                                    id BIGINT NOT NULL AUTO_INCREMENT,
                                    follower_id BIGINT NOT NULL,
                                    followed_id BIGINT NOT NULL,
                                    followed_at DATETIME(6) NULL,
                                    PRIMARY KEY (id),
                                    UNIQUE KEY uk_follow_connections_follower_followed (follower_id, followed_id),
                                    KEY idx_follow_connections_followed_id (followed_id),
                                    CONSTRAINT fk_follow_connections_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_follow_connections_followed FOREIGN KEY (followed_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE candidate_skills (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  candidate_id BIGINT NOT NULL,
                                  skill_name VARCHAR(100) NOT NULL,
                                  level VARCHAR(30) NULL,
                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_candidate_skills_candidate_skill_name (candidate_id, skill_name),
                                  KEY idx_candidate_skills_skill_name (skill_name),
                                  CONSTRAINT fk_candidate_skills_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE educations (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            candidate_id BIGINT NOT NULL,
                            school VARCHAR(200) NOT NULL,
                            major VARCHAR(150) NULL,
                            degree VARCHAR(50) NULL,
                            start_date DATE NULL,
                            end_date DATE NULL,
                            description TEXT NULL,
                            PRIMARY KEY (id),
                            KEY idx_educations_candidate_id (candidate_id),
                            CONSTRAINT fk_educations_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE work_experiences (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  candidate_id BIGINT NOT NULL,
                                  company VARCHAR(200) NOT NULL,
                                  position VARCHAR(150) NOT NULL,
                                  start_date DATE NULL,
                                  end_date DATE NULL,
                                  description TEXT NULL,
                                  currently_working BIT(1) NULL,
                                  PRIMARY KEY (id),
                                  KEY idx_work_experiences_candidate_id (candidate_id),
                                  CONSTRAINT fk_work_experiences_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chat_rooms (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            user_one_id BIGINT NOT NULL,
                            user_two_id BIGINT NOT NULL,
                            created_at DATETIME(6) NULL,
                            last_message_at DATETIME(6) NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_chat_rooms_user_one_user_two (user_one_id, user_two_id),
                            KEY idx_chat_rooms_user_two_id (user_two_id),
                            KEY idx_chat_rooms_last_message_at (last_message_at),
                            CONSTRAINT fk_chat_rooms_user_one FOREIGN KEY (user_one_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_chat_rooms_user_two FOREIGN KEY (user_two_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chat_messages (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               room_id BIGINT NOT NULL,
                               sender_id BIGINT NOT NULL,
                               content TEXT NOT NULL,
                               is_read BIT(1) NOT NULL,
                               sent_at DATETIME(6) NULL,
                               PRIMARY KEY (id),
                               KEY idx_chat_messages_room_id (room_id),
                               KEY idx_chat_messages_sender_id (sender_id),
                               KEY idx_chat_messages_room_sent_at (room_id, sent_at),
                               CONSTRAINT fk_chat_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
                               CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
