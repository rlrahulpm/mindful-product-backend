CREATE TABLE user_stories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    epic_id VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    acceptance_criteria TEXT,
    priority VARCHAR(20) DEFAULT 'Medium',
    story_points INT,
    status VARCHAR(20) DEFAULT 'Draft',
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,

    CONSTRAINT fk_user_stories_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    INDEX idx_epic_id (epic_id),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status)
);

-- Add check constraint for priority values
ALTER TABLE user_stories
ADD CONSTRAINT chk_priority
CHECK (priority IN ('High', 'Medium', 'Low'));

-- Add check constraint for status values
ALTER TABLE user_stories
ADD CONSTRAINT chk_status
CHECK (status IN ('Draft', 'Ready', 'In Progress', 'Done', 'Blocked'));