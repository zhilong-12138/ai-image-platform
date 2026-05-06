-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_image_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_image_db;

-- 1. 用户表
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL COMMENT '邮箱',
  password VARCHAR(255) COMMENT '密码 (BCrypt)',
  level INT DEFAULT 1 COMMENT '用户等级',
  invite_code VARCHAR(20) UNIQUE COMMENT '用户邀请码',
  invitee_id BIGINT COMMENT '邀请人ID (nullable)',
  status TINYINT DEFAULT 1 COMMENT '1=正常 0=禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_email (email),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 分类表
CREATE TABLE category (
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

-- 3. 提示词表
CREATE TABLE prompt (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT COMMENT '分类ID',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  description TEXT COMMENT '描述',
  content TEXT NOT NULL COMMENT '提示词内容',
  image_urls TEXT COMMENT '示意图URL列表 (JSON格式)',
  params TEXT COMMENT '参数JSON (合参数配置)',
  life_cycle INT COMMENT '生成~完成(秒)',
  api_url VARCHAR(500) COMMENT '调用API地址',
  api_method VARCHAR(20) COMMENT 'GET/POST',
  api_params TEXT COMMENT '参数配置 (JSON编码器或结构)',
  cost_points INT DEFAULT 10 COMMENT '生成消耗积分',
  ref_image_count INT DEFAULT 0 COMMENT '参考图片数量',
  sort INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '1=上线 0=下架',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_status (status),
  INDEX idx_sort (sort),
  INDEX idx_category_id (category_id),
  FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 生成记录表
CREATE TABLE generation_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  prompt_id BIGINT NOT NULL COMMENT '提示词ID',
  params TEXT COMMENT '用户选择的参数 (JSON)',
  ref_images TEXT COMMENT '参考图片URLs (JSON)',
  status TINYINT DEFAULT 0 COMMENT '0=待处理 1=生成中 2=生成完成 3=失败',
  result_images TEXT COMMENT '生成的图片URLs (JSON)',
  external_api_response TEXT COMMENT '外部API响应 (JSON, 保留原始响应)',
  error_msg VARCHAR(500) COMMENT '失败原因',
  cost_points INT COMMENT '消耗积分',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (user_id),
  INDEX idx_prompt_id (prompt_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at),
  FOREIGN KEY (user_id) REFERENCES sys_user(id),
  FOREIGN KEY (prompt_id) REFERENCES prompt(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 积分日志表
CREATE TABLE point_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  type TINYINT NOT NULL COMMENT '1=生成消费 2=邀请奖励 3=充值 4=管理员调整',
  amount INT NOT NULL COMMENT '变动积分 (正数=增加 负数=减少)',
  balance INT NOT NULL COMMENT '变动后余额',
  related_id BIGINT COMMENT '关联记录ID (generation_record.id)',
  remark VARCHAR(255) COMMENT '备注',
  is_settled TINYINT DEFAULT 0 COMMENT '0=未结算 1=已结算',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id (user_id),
  INDEX idx_type (type),
  INDEX idx_created_at (created_at),
  FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 收藏表
CREATE TABLE favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  prompt_id BIGINT NOT NULL COMMENT '提示词ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  UNIQUE KEY uk_user_prompt (user_id, prompt_id),
  INDEX idx_user_id (user_id),
  FOREIGN KEY (user_id) REFERENCES sys_user(id),
  FOREIGN KEY (prompt_id) REFERENCES prompt(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 邮箱验证码表
CREATE TABLE email_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL COMMENT '邮箱',
  code VARCHAR(10) NOT NULL COMMENT '验证码',
  expire_at DATETIME NOT NULL COMMENT '过期时间',
  used TINYINT DEFAULT 0 COMMENT '0=未用 1=已用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_email (email),
  INDEX idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 系统配置表
CREATE TABLE system_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键 (一一对应)',
  config_value VARCHAR(500) NOT NULL COMMENT '配置值',
  description VARCHAR(255) COMMENT '配置说明',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('init_points', '100', '新用户初始积分'),
('invite_points', '10', '邀请用户获得积分'),
('invited_points', '10', '被邀请用户获得积分'),
('api_key', 'marsware.ai', '外部API Key'),
('oss_base_url', 'https://oss.example.com', 'OSS 公开访问 URL');
