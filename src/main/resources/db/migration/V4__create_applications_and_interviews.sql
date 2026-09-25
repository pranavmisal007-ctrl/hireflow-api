-- V4: Applications and Interviews tables

CREATE TABLE IF NOT EXISTS resume_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seeker_profile_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    s3_url VARCHAR(1000) NOT NULL,
    file_size_bytes BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_primary BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_rf_profile FOREIGN KEY (seeker_profile_id) REFERENCES seeker_profiles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    seeker_id BIGINT NOT NULL,
    cover_letter TEXT,
    resume_file_id BIGINT,
    status ENUM('APPLIED', 'SHORTLISTED', 'REJECTED', 'HIRED') NOT NULL DEFAULT 'APPLIED',
    match_score DOUBLE DEFAULT 0.0,
    recruiter_notes TEXT,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_seeker FOREIGN KEY (seeker_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_resume FOREIGN KEY (resume_file_id) REFERENCES resume_files(id) ON DELETE SET NULL,
    CONSTRAINT uq_seeker_job UNIQUE (seeker_id, job_id)
);

CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_seeker ON applications(seeker_id);

CREATE TABLE IF NOT EXISTS interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INT DEFAULT 60,
    type ENUM('PHONE', 'VIDEO', 'ONSITE', 'TECHNICAL') NOT NULL,
    meeting_link VARCHAR(500),
    venue VARCHAR(300),
    notes TEXT,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    CONSTRAINT fk_int_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);
