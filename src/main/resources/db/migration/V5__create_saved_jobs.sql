-- V5: Saved Jobs table

CREATE TABLE IF NOT EXISTS saved_jobs (
    id BIGSERIAL PRIMARY KEY,
    seeker_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sj_seeker FOREIGN KEY (seeker_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sj_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT uq_seeker_saved_job UNIQUE (seeker_id, job_id)
);
