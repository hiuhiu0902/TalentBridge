CREATE TABLE application_histories
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    application_id    BIGINT      NOT NULL,
    action_by_user_id BIGINT NULL,
    from_status       VARCHAR(30) NOT NULL,
    to_status         VARCHAR(30) NOT NULL,
    note              TEXT NULL,
    changed_at        datetime NULL,
    CONSTRAINT pk_application_histories PRIMARY KEY (id)
);

CREATE TABLE applications
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    candidate_id   BIGINT      NOT NULL,
    job_post_id    BIGINT      NOT NULL,
    status         VARCHAR(30) NOT NULL,
    cv_url_at_time VARCHAR(255) NULL,
    cover_letter   TEXT NULL,
    applied_at     datetime NULL,
    CONSTRAINT pk_applications PRIMARY KEY (id)
);

CREATE TABLE candidate_skills
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    candidate_id BIGINT       NOT NULL,
    skill_name   VARCHAR(100) NOT NULL,
    level        VARCHAR(30) NULL,
    CONSTRAINT pk_candidate_skills PRIMARY KEY (id)
);

CREATE TABLE candidates
(
    user_id    BIGINT NOT NULL,
    phone      VARCHAR(20) NULL,
    address    VARCHAR(255) NULL,
    summary    TEXT NULL,
    cv_url     VARCHAR(255) NULL,
    avatar_url VARCHAR(255) NULL,
    CONSTRAINT pk_candidates PRIMARY KEY (user_id)
);

CREATE TABLE categories
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE chat_messages
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    room_id   BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content   TEXT   NOT NULL,
    is_read   BIT(1) NOT NULL,
    sent_at   datetime NULL,
    CONSTRAINT pk_chat_messages PRIMARY KEY (id)
);

CREATE TABLE chat_rooms
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    user_one_id     BIGINT NOT NULL,
    user_two_id     BIGINT NOT NULL,
    created_at      datetime NULL,
    last_message_at datetime NULL,
    CONSTRAINT pk_chat_rooms PRIMARY KEY (id)
);

CREATE TABLE educations
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    candidate_id  BIGINT       NOT NULL,
    school        VARCHAR(200) NOT NULL,
    major         VARCHAR(150) NULL,
    degree        VARCHAR(50) NULL,
    start_date    date NULL,
    end_date      date NULL,
    `description` TEXT NULL,
    CONSTRAINT pk_educations PRIMARY KEY (id)
);

CREATE TABLE employers
(
    user_id       BIGINT       NOT NULL,
    company_name  VARCHAR(200) NOT NULL,
    website       VARCHAR(255) NULL,
    `description` TEXT NULL,
    logo_url      VARCHAR(255) NULL,
    industry      VARCHAR(100) NULL,
    company_size  VARCHAR(100) NULL,
    address       VARCHAR(255) NULL,
    CONSTRAINT pk_employers PRIMARY KEY (user_id)
);

CREATE TABLE follow_connections
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    follower_id BIGINT NOT NULL,
    followed_id BIGINT NOT NULL,
    followed_at datetime NULL,
    CONSTRAINT pk_follow_connections PRIMARY KEY (id)
);

CREATE TABLE interviews
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    application_id BIGINT NOT NULL,
    interview_at   datetime NULL,
    location       VARCHAR(255) NULL,
    meeting_link   VARCHAR(255) NULL,
    note           TEXT NULL,
    status         VARCHAR(30) NULL,
    created_at     datetime NULL,
    CONSTRAINT pk_interviews PRIMARY KEY (id)
);

CREATE TABLE job_posts
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    employer_id      BIGINT       NOT NULL,
    category_id      BIGINT NULL,
    title            VARCHAR(200) NOT NULL,
    `description`    TEXT NULL,
    salary_min       DECIMAL NULL,
    salary_max       DECIMAL NULL,
    location         VARCHAR(100) NULL,
    job_type         VARCHAR(50) NULL,
    experience_level VARCHAR(50) NULL,
    status           VARCHAR(30)  NOT NULL,
    posted_at        datetime NULL,
    expired_at       datetime NULL,
    rejection_reason TEXT NULL,
    CONSTRAINT pk_job_posts PRIMARY KEY (id)
);

CREATE TABLE job_skills
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    job_post_id BIGINT       NOT NULL,
    skill_name  VARCHAR(100) NOT NULL,
    level       VARCHAR(30) NULL,
    CONSTRAINT pk_job_skills PRIMARY KEY (id)
);

CREATE TABLE notifications
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    user_id       BIGINT       NOT NULL,
    title         VARCHAR(255) NOT NULL,
    content       TEXT NULL,
    type          VARCHAR(50) NULL,
    reference_url VARCHAR(255) NULL,
    is_read       BIT(1)       NOT NULL,
    created_at    datetime NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE TABLE saved_jobs
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    candidate_id BIGINT NOT NULL,
    job_post_id  BIGINT NOT NULL,
    saved_at     datetime NULL,
    CONSTRAINT pk_saved_jobs PRIMARY KEY (id)
);

CREATE TABLE users
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    full_name   VARCHAR(100) NULL,
    password    VARCHAR(255) NULL,
    `role`      VARCHAR(20)  NOT NULL,
    active      BIT(1)       NOT NULL,
    avatar_url  VARCHAR(255) NULL,
    provider    VARCHAR(50) NULL,
    provider_id VARCHAR(255) NULL,
    created_at  datetime NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE work_experiences
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    candidate_id      BIGINT       NOT NULL,
    company           VARCHAR(200) NOT NULL,
    position          VARCHAR(150) NOT NULL,
    start_date        date NULL,
    end_date          date NULL,
    `description`     TEXT NULL,
    currently_working BIT(1) NULL,
    CONSTRAINT pk_work_experiences PRIMARY KEY (id)
);

ALTER TABLE candidate_skills
    ADD CONSTRAINT uc_2eed8e08cc9222b0a59cebd54 UNIQUE (candidate_id, skill_name);

ALTER TABLE job_skills
    ADD CONSTRAINT uc_44bf974d64c5c06f8a09d511a UNIQUE (job_post_id, skill_name);

ALTER TABLE follow_connections
    ADD CONSTRAINT uc_69f2d0ec9c5a04b564342ffba UNIQUE (follower_id, followed_id);

ALTER TABLE applications
    ADD CONSTRAINT uc_7118ec29b417e5e0c4e3cc968 UNIQUE (candidate_id, job_post_id);

ALTER TABLE categories
    ADD CONSTRAINT uc_categories_name UNIQUE (name);

ALTER TABLE chat_rooms
    ADD CONSTRAINT uc_d11e11efac2489870e3a4b2f5 UNIQUE (user_one_id, user_two_id);

ALTER TABLE saved_jobs
    ADD CONSTRAINT uc_e76c5b5888980970175efcd1e UNIQUE (candidate_id, job_post_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE applications
    ADD CONSTRAINT FK_APPLICATIONS_ON_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES candidates (user_id);

ALTER TABLE applications
    ADD CONSTRAINT FK_APPLICATIONS_ON_JOB_POST FOREIGN KEY (job_post_id) REFERENCES job_posts (id);

ALTER TABLE application_histories
    ADD CONSTRAINT FK_APPLICATION_HISTORIES_ON_ACTION_BY_USER FOREIGN KEY (action_by_user_id) REFERENCES users (id);

ALTER TABLE application_histories
    ADD CONSTRAINT FK_APPLICATION_HISTORIES_ON_APPLICATION FOREIGN KEY (application_id) REFERENCES applications (id);

ALTER TABLE candidates
    ADD CONSTRAINT FK_CANDIDATES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE candidate_skills
    ADD CONSTRAINT FK_CANDIDATE_SKILLS_ON_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES candidates (user_id);

ALTER TABLE chat_messages
    ADD CONSTRAINT FK_CHAT_MESSAGES_ON_ROOM FOREIGN KEY (room_id) REFERENCES chat_rooms (id);

ALTER TABLE chat_messages
    ADD CONSTRAINT FK_CHAT_MESSAGES_ON_SENDER FOREIGN KEY (sender_id) REFERENCES users (id);

ALTER TABLE chat_rooms
    ADD CONSTRAINT FK_CHAT_ROOMS_ON_USER_ONE FOREIGN KEY (user_one_id) REFERENCES users (id);

ALTER TABLE chat_rooms
    ADD CONSTRAINT FK_CHAT_ROOMS_ON_USER_TWO FOREIGN KEY (user_two_id) REFERENCES users (id);

ALTER TABLE educations
    ADD CONSTRAINT FK_EDUCATIONS_ON_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES candidates (user_id);

ALTER TABLE employers
    ADD CONSTRAINT FK_EMPLOYERS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE follow_connections
    ADD CONSTRAINT FK_FOLLOW_CONNECTIONS_ON_FOLLOWED FOREIGN KEY (followed_id) REFERENCES users (id);

ALTER TABLE follow_connections
    ADD CONSTRAINT FK_FOLLOW_CONNECTIONS_ON_FOLLOWER FOREIGN KEY (follower_id) REFERENCES users (id);

ALTER TABLE interviews
    ADD CONSTRAINT FK_INTERVIEWS_ON_APPLICATION FOREIGN KEY (application_id) REFERENCES applications (id);

ALTER TABLE job_posts
    ADD CONSTRAINT FK_JOB_POSTS_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE job_posts
    ADD CONSTRAINT FK_JOB_POSTS_ON_EMPLOYER FOREIGN KEY (employer_id) REFERENCES employers (user_id);

ALTER TABLE job_skills
    ADD CONSTRAINT FK_JOB_SKILLS_ON_JOB_POST FOREIGN KEY (job_post_id) REFERENCES job_posts (id);

ALTER TABLE notifications
    ADD CONSTRAINT FK_NOTIFICATIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE saved_jobs
    ADD CONSTRAINT FK_SAVED_JOBS_ON_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES candidates (user_id);

ALTER TABLE saved_jobs
    ADD CONSTRAINT FK_SAVED_JOBS_ON_JOB_POST FOREIGN KEY (job_post_id) REFERENCES job_posts (id);

ALTER TABLE work_experiences
    ADD CONSTRAINT FK_WORK_EXPERIENCES_ON_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES candidates (user_id);