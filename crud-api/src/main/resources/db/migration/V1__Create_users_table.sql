CREATE TABLE users (
                       uuid CHAR(36) PRIMARY KEY,
                       fullname VARCHAR(255) NOT NULL,
                       study_level VARCHAR(255) NOT NULL,
                       age INT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_fullname ON users(fullname);
CREATE INDEX idx_users_study_level ON users(study_level);