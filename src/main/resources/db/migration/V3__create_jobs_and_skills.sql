-- V3: Jobs and Skills tables

CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recruiter_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(250) NOT NULL UNIQUE,
    description TEXT,
    requirements TEXT,
    location VARCHAR(100),
    is_remote BOOLEAN NOT NULL DEFAULT FALSE,
    job_type VARCHAR(50),
    experience_level VARCHAR(50),
    salary_min BIGINT,
    salary_max BIGINT,
    currency VARCHAR(10) DEFAULT 'INR',
    application_deadline DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    views_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_recruiter FOREIGN KEY (recruiter_id) REFERENCES recruiter_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE INDEX idx_jobs_is_active ON jobs(is_active);
CREATE INDEX idx_jobs_job_type ON jobs(job_type);
CREATE INDEX idx_jobs_experience_level ON jobs(experience_level);
CREATE INDEX idx_jobs_location ON jobs(location);

CREATE TABLE IF NOT EXISTS job_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_js_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS seeker_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seeker_profile_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency VARCHAR(50),
    years_used INT,
    CONSTRAINT fk_ss_profile FOREIGN KEY (seeker_profile_id) REFERENCES seeker_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_ss_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS experiences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seeker_profile_id BIGINT NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    title VARCHAR(200) NOT NULL,
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    description TEXT,
    CONSTRAINT fk_exp_profile FOREIGN KEY (seeker_profile_id) REFERENCES seeker_profiles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS educations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seeker_profile_id BIGINT NOT NULL,
    institution VARCHAR(200) NOT NULL,
    degree VARCHAR(100) NOT NULL,
    field_of_study VARCHAR(100),
    start_year INT,
    end_year INT,
    grade VARCHAR(20),
    CONSTRAINT fk_edu_profile FOREIGN KEY (seeker_profile_id) REFERENCES seeker_profiles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seeker_profile_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    tech_stack VARCHAR(500),
    project_url VARCHAR(255),
    repo_url VARCHAR(255),
    CONSTRAINT fk_proj_profile FOREIGN KEY (seeker_profile_id) REFERENCES seeker_profiles(id) ON DELETE CASCADE
);
