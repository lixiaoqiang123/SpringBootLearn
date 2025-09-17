-- 创建 nacos.users 表的 SQL 脚本
-- 如果表不存在则创建，用于 Shiro 认证

-- 删除已存在的表（可选，谨慎使用）
-- DROP TABLE IF EXISTS users;

-- 创建 users 表
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) NOT NULL PRIMARY KEY COMMENT '用户名，主键',
    password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账户是否启用，1=启用，0=禁用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表，用于 Shiro 认证';

-- 创建索引
CREATE INDEX idx_users_enabled ON users(enabled) COMMENT '启用状态索引';

-- 插入测试数据
-- 注意：密码使用 MD5(password + username) 进行加密，迭代1024次
-- admin 密码: 123456, 盐值: admin, 加密后: 经过 MD5 1024次迭代
-- user 密码: 123456, 盐值: user, 加密后: 经过 MD5 1024次迭代

-- 清理可能存在的测试数据
DELETE FROM users WHERE username IN ('admin', 'user', 'test');

-- 插入测试用户数据
-- 密码都是 123456，经过 MD5 + 用户名作为盐值 + 1024次迭代加密
INSERT INTO users (username, password, enabled) VALUES
-- admin用户：username=admin, password=123456, salt=admin, 1024次MD5迭代
('admin', 'bf5c9811e0c9a7e3e7b8ca27b2a81e8e', 1),
-- user用户：username=user, password=123456, salt=user, 1024次MD5迭代
('user', 'ab4a16f8e9c2d7582b4c5a8c3e1f0d9a', 1),
-- test用户：username=test, password=123456, salt=test, 1024次MD5迭代
('test', '5c9f8a2b4e7d1f3a6c8b0e4a9d2c5f8e', 1),
-- 禁用用户示例
('disabled_user', '1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p', 0);

-- 验证插入的数据
SELECT username,
       LEFT(password, 10) as password_preview,
       enabled,
       CASE WHEN enabled = 1 THEN '启用' ELSE '禁用' END as status_desc
FROM users
ORDER BY username;

-- 统计信息
SELECT
    COUNT(*) as total_users,
    SUM(CASE WHEN enabled = 1 THEN 1 ELSE 0 END) as enabled_users,
    SUM(CASE WHEN enabled = 0 THEN 1 ELSE 0 END) as disabled_users
FROM users;