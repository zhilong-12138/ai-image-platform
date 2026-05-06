CREATE TABLE IF NOT EXISTS category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '分类名称',
  code VARCHAR(100) UNIQUE NOT NULL COMMENT '分类编码',
  sort INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '1=启用 0=禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_status (status),
  INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE prompt
  ADD COLUMN category_id BIGINT NULL COMMENT '分类ID';

CREATE INDEX idx_category_id ON prompt (category_id);

ALTER TABLE prompt
  ADD CONSTRAINT fk_prompt_category
  FOREIGN KEY (category_id) REFERENCES category(id);
