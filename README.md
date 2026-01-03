# Demo Server - Spring Boot Multi-Module Project

Java 17 기반 Spring Boot 멀티모듈 프로젝트입니다.

## 프로젝트 구조

```
demo-server/
├── demo-common/           # 공통 기능 모듈
│   ├── dto/              # 공통 DTO (BaseResponse 등)
│   └── exception/        # 공통 예외 처리
├── demo-security/        # Spring Security 및 JWT 모듈
│   ├── jwt/             # JWT 토큰 관련 기능
│   └── config/          # Security 설정
└── demo-auth/           # 인증/인가 서비스 모듈 (Spring Boot Application)
    ├── domain/          # 도메인 엔티티
    ├── repository/      # JPA Repository
    ├── service/         # 비즈니스 로직
    ├── controller/      # REST API 컨트롤러
    └── dto/             # 요청/응답 DTO
```

## 기술 스택

- Java 17
- Spring Boot 3.2.1
- Spring Security
- Spring Data JPA
- MyBatis (선택적)
- JWT (io.jsonwebtoken)
- H2 Database (개발용)
- MySQL (운영용)
- Lombok
- Validation

## 빌드 및 실행

### 1. MySQL 데이터베이스 설정

#### Windows
```bash
cd scripts
init-mysql.bat
```

#### Linux/Mac
```bash
cd scripts
chmod +x init-mysql.sh
./init-mysql.sh
```

또는 직접 MySQL 명령어로:
```bash
mysql -u root -p < scripts/init-mysql.sql
```

### 2. 전체 프로젝트 빌드
```bash
mvn clean install
```

### 3. demo-auth 애플리케이션 실행
```bash
cd demo-auth
mvn spring-boot:run
```

또는

```bash
java -jar demo-auth/target/demo-auth-1.0-SNAPSHOT.jar
```

### 4. 프로파일별 실행
```bash
# 개발 환경 (기본)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 운영 환경
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## API 엔드포인트

### 회원가입
```bash
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

### 로그인
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "success": true,
  "message": "로그인이 완료되었습니다",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer"
  }
}
```

### 토큰 갱신
```bash
POST /api/auth/refresh
Refresh-Token: {refreshToken}
```

## 모듈 설명

### demo-common
- 공통 Response DTO (`BaseResponse`)
- 공통 예외 처리 (`BusinessException`, `ErrorCode`)
- 글로벌 예외 핸들러 (`GlobalExceptionHandler`)

### demo-security
- JWT 토큰 생성 및 검증 (`JwtTokenProvider`)
- JWT 인증 필터 (`JwtAuthenticationFilter`)
- Security 설정 (`SecurityConfig`)
- 인증/인가 예외 핸들러

### demo-auth
- 사용자 도메인 및 Repository
- 회원가입/로그인 비즈니스 로직
- 인증 API 컨트롤러

## 설정

### JWT 설정 (application.yml)
- `jwt.secret`: JWT 서명에 사용되는 비밀키 (운영환경에서는 반드시 변경)
- `jwt.access-token-validity`: Access Token 유효기간 (기본 1시간)
- `jwt.refresh-token-validity`: Refresh Token 유효기간 (기본 7일)

### 데이터베이스 설정

#### MySQL 데이터베이스 정보
- **데이터베이스명**: demo_db
- **기본 사용자**: root
- **기본 비밀번호**: root (변경 권장)
- **문자셋**: utf8mb4
- **접속 URL**: jdbc:mysql://localhost:3306/demo_db

#### application.yml 설정
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo_db
    username: root
    password: root  # 운영환경에서는 반드시 변경
```

**주의**: 운영환경에서는 반드시 강력한 비밀번호로 변경하고, 전용 사용자를 생성하세요.

## 추가 모듈 생성 방법

새로운 서비스 모듈을 추가할 때:

1. 루트 pom.xml의 `<modules>` 섹션에 모듈 추가
2. 새 모듈 디렉토리 생성 및 pom.xml 작성
3. `demo-common` 및 `demo-security` 의존성 추가
4. Spring Boot Application 클래스 생성 시 ComponentScan 설정:
   ```java
   @ComponentScan(basePackages = {"com.example.{your-module}", "com.example.common", "com.example.security"})
   ```

## 주의사항

### 보안
- 운영환경 배포 시 반드시 `jwt.secret` 값을 안전한 값으로 변경하세요
- MySQL 비밀번호를 강력한 비밀번호로 변경하세요
- 운영환경에서는 `application-prod.yml`을 사용하세요: `--spring.profiles.active=prod`

### 데이터베이스
- MySQL 서버가 실행 중인지 확인하세요
- demo_db 데이터베이스가 생성되어 있어야 합니다
- JPA의 `ddl-auto: update` 설정으로 테이블은 자동 생성됩니다
- 운영환경에서는 `ddl-auto: validate`로 변경하는 것을 권장합니다
