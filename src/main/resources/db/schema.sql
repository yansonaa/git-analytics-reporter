-- 提交记录表
CREATE TABLE IF NOT EXISTS commit_record (
    commit_id VARCHAR(40) PRIMARY KEY,
    project_id VARCHAR(64) NOT NULL,
    author_email VARCHAR(255) NOT NULL,
    author_name VARCHAR(100),
    commit_time TIMESTAMP NOT NULL,
    add_lines INT DEFAULT 0,
    delete_lines INT DEFAULT 0,
    net_lines INT DEFAULT 0,
    file_count INT DEFAULT 0,
    message VARCHAR(1000),
    is_merge_commit BOOLEAN DEFAULT FALSE,
    is_automated BOOLEAN DEFAULT FALSE,
    is_anomaly BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_commit_project_time ON commit_record(project_id, commit_time);
CREATE INDEX IF NOT EXISTS idx_commit_author_time ON commit_record(author_email, commit_time);
CREATE INDEX IF NOT EXISTS idx_commit_time ON commit_record(commit_time);

-- 评审记录表
CREATE TABLE IF NOT EXISTS review_record (
    id BIGSERIAL PRIMARY KEY,
    mr_id VARCHAR(64) NOT NULL,
    reviewer_email VARCHAR(255) NOT NULL,
    review_time TIMESTAMP NOT NULL,
    comment_count INT DEFAULT 0,
    response_seconds INT,
    project_id VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_review_project_time ON review_record(project_id, review_time);
CREATE INDEX IF NOT EXISTS idx_review_reviewer ON review_record(reviewer_email);

-- 团队成员配置表
CREATE TABLE IF NOT EXISTS team_member (
    email VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100),
    team_id VARCHAR(64),
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_team_member_team ON team_member(team_id);
