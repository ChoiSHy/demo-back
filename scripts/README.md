# MySQL 데이터베이스 설정 가이드

## 데이터베이스 정보

- **데이터베이스명**: `demo_db`
- **문자셋**: `utf8mb4`
- **Collation**: `utf8mb4_unicode_ci`
- **기본 사용자**: `root`
- **기본 비밀번호**: `root`

## 데이터베이스 초기화

### 방법 1: 스크립트 실행 (권장)

#### Windows
```bash
# scripts 디렉토리로 이동
cd scripts

# 초기화 스크립트 실행
init-mysql.bat

# MySQL 비밀번호 입력
```

#### Linux/Mac
```bash
# scripts 디렉토리로 이동
cd scripts

# 실행 권한 부여
chmod +x init-mysql.sh

# 초기화 스크립트 실행
./init-mysql.sh

# MySQL 비밀번호 입력
```

### 방법 2: MySQL 명령어 직접 실행

```bash
mysql -u root -p < init-mysql.sql
```

### 방법 3: MySQL CLI에서 수동 실행

```bash
# MySQL 접속
mysql -u root -p

# 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS demo_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

# 데이터베이스 사용
USE demo_db;

# 확인
SHOW DATABASES LIKE 'demo_db';
```

## 전용 사용자 생성 (권장)

보안을 위해 root 대신 전용 사용자를 생성하는 것을 권장합니다:

```sql
-- MySQL 접속
mysql -u root -p

-- 사용자 생성
CREATE USER IF NOT EXISTS 'demo_user'@'localhost' IDENTIFIED BY 'your_strong_password';

-- 권한 부여
GRANT ALL PRIVILEGES ON demo_db.* TO 'demo_user'@'localhost';

-- 권한 적용
FLUSH PRIVILEGES;

-- 확인
SHOW GRANTS FOR 'demo_user'@'localhost';
```

전용 사용자를 생성한 후 `application.yml` 수정:

```yaml
spring:
  datasource:
    username: demo_user
    password: your_strong_password
```

## 테이블 확인

애플리케이션이 처음 실행되면 JPA가 자동으로 테이블을 생성합니다.

```sql
-- MySQL 접속
mysql -u root -p

-- demo_db 선택
USE demo_db;

-- 테이블 목록 확인
SHOW TABLES;

-- users 테이블 구조 확인
DESC users;

-- 데이터 확인
SELECT * FROM users;
```

## 데이터베이스 삭제 (필요시)

```sql
-- MySQL 접속
mysql -u root -p

-- 데이터베이스 삭제
DROP DATABASE IF EXISTS demo_db;

-- 사용자 삭제 (전용 사용자를 생성한 경우)
DROP USER IF EXISTS 'demo_user'@'localhost';
```

## 문제 해결

### MySQL 서버가 실행되지 않는 경우

#### Windows
```bash
# 서비스 시작
net start MySQL80

# 또는 서비스 관리자에서 MySQL 서비스 시작
```

#### Linux
```bash
# 서비스 시작
sudo systemctl start mysql

# 상태 확인
sudo systemctl status mysql
```

#### Mac
```bash
# Homebrew로 설치한 경우
brew services start mysql

# 상태 확인
brew services list
```

### 접속 오류 (Access denied)

1. MySQL 비밀번호 확인
2. 사용자 권한 확인
3. 호스트 설정 확인 (`localhost` vs `127.0.0.1`)

### 타임존 오류

`application.yml`의 `serverTimezone` 설정 확인:
```yaml
url: jdbc:mysql://localhost:3306/demo_db?serverTimezone=Asia/Seoul
```

### 문자셋 오류

MySQL 설정 파일(`my.cnf` 또는 `my.ini`)에서 다음 확인:
```ini
[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4

[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
```

## 백업 및 복원

### 백업
```bash
mysqldump -u root -p demo_db > demo_db_backup.sql
```

### 복원
```bash
mysql -u root -p demo_db < demo_db_backup.sql
```
