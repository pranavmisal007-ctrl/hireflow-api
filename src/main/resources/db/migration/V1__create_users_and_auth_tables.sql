-- V1: Users and Auth tables

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin user (password: Admin@1234)
INSERT INTO users (email, password, role, is_active)
VALUES ('admin@hireflow.dev', '$2a$12$wXqFJlbWDBdNdeBLo7Ar8.GzJYj3UxqVLrV4qpxwH4DhJl8vy7F1y', 'ADMIN', TRUE)
ON CONFLICT (email) DO NOTHING;
