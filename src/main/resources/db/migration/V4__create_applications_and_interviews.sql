-- V4: Applications and Interviews tables


CREATE TABLE IF NOT EXISTS applications (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    seeker_id BIGINT NOT NULL,
    cover_letter TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    match_score DOUBLE PRECISION DEFAULT 0.0,
    recruiter_notes TEXT,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_seeker FOREIGN KEY (seeker_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_seeker_job UNIQUE (seeker_id, job_id)
);

CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_seeker ON applications(seeker_id);

CREATE TABLE IF NOT EXISTS interviews (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INT DEFAULT 60,
    type VARCHAR(50) NOT NULL,
    meeting_link VARCHAR(500),
    venue VARCHAR(300),
    notes TEXT,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    CONSTRAINT fk_int_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);
