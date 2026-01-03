-- ===================================
-- MySQL Database Initialization Script
-- ===================================

-- 1. 데이터베이스 생성 (존재하지 않는 경우)
CREATE DATABASE IF NOT EXISTS demo_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. 데이터베이스 사용
USE demo_db;

-- 3. 사용자 생성 (선택사항 - 보안을 위해 root 대신 전용 사용자 사용)
-- CREATE USER IF NOT EXISTS 'demo_user'@'localhost' IDENTIFIED BY 'demo_password';
-- GRANT ALL PRIVILEGES ON demo_db.* TO 'demo_user'@'localhost';
-- FLUSH PRIVILEGES;

-- 4. 테이블은 JPA가 자동 생성하므로 별도 생성 불필요
-- hibernate.ddl-auto=update 설정으로 자동 생성됨

-- 5. 데이터베이스 확인
SELECT 'Database demo_db created successfully!' AS message;
SHOW DATABASES LIKE 'demo_db';
