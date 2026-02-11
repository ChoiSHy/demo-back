# Spring Boot Error Auto-Detector v1.0

Spring Boot 런타임 에러를 자동으로 감지하고, 패턴 매칭 + AI(Claude API)로 원인 분석 및 수정 제안을 제공하는 도구입니다.

---

## 프로젝트 구조

```
demo-server/
├── .vscode/
│   ├── launch.json          ← F5 디버그 구성 (4개)
│   └── tasks.json           ← Error Detector 워치 태스크
├── demo-auth/               ← 인증 서비스 (포트 81)
├── demo-system/             ← 시스템 API 서비스 (포트 82)
├── error-detector/
│   ├── .env                 ← ANTHROPIC_API_KEY 설정
│   ├── package.json
│   └── src/
│       ├── detector.js      ← 메인 진입점
│       ├── log-parser.js    ← 로그 파싱 엔진
│       ├── pattern-analyzer.js  ← 1차: 패턴 매칭 분석
│       ├── ai-analyzer.js   ← 2차: Claude AI 분석
│       └── display.js       ← 터미널 출력 포맷터
└── logs/                    ← Spring Boot 로그 파일 출력 경로
```

---

## 사전 준비

### 1. 필수 환경

| 항목 | 요구사항 |
|------|----------|
| Node.js | v18 이상 |
| Java | JDK 17 |
| VSCode 확장 | Extension Pack for Java (vscjava.vscode-java-pack) |
| Maven | 시스템 Maven (`mvn` 명령어 사용 가능) |

### 2. 의존성 설치

```bash
cd demo-server/error-detector
npm install
```

### 3. AI 분석 활성화 (선택)

`error-detector/.env` 파일에 API 키를 설정하면 패턴 매칭으로 해결되지 않는 에러를 Claude가 추가 분석합니다.

```
ANTHROPIC_API_KEY=sk-ant-xxxxx
```

> API 키가 없어도 패턴 매칭 분석은 정상 동작합니다.

---

## 실행 방법

### 방법 1: VSCode F5 (권장)

1. VSCode에서 `demo-server/` 폴더를 엽니다
2. **F5** (또는 `Ctrl+Shift+D` → 실행 버튼) 클릭
3. 드롭다운에서 원하는 구성을 선택합니다:

| 구성 이름 | 설명 |
|-----------|------|
| **demo-auth + Error Detector** | 인증 서비스 + 에러 감지 |
| **demo-system + Error Detector** | 시스템 API + 에러 감지 |
| demo-auth (standalone) | 인증 서비스만 실행 (에러 감지 없음) |
| demo-system (standalone) | 시스템 API만 실행 (에러 감지 없음) |

**동작 흐름:**
1. Error Detector가 전용 터미널 패널에서 자동 시작
2. 로그 파일이 아직 없으면 "로그 파일 대기 중..." 메시지 출력
3. Spring Boot 앱이 디버그 모드로 시작
4. Spring Boot가 로그 파일을 생성하면 모니터링 시작
5. 에러 발생 시 분석 결과가 Error Detector 터미널에 표시

### 방법 2: 터미널 수동 실행

**로그 파일 감시 모드** — 터미널 2개 필요:

```bash
# 터미널 1: Error Detector 시작
cd demo-server/error-detector
npm run watch:auth          # demo-auth 로그 감시
# 또는
npm run watch:system        # demo-system 로그 감시

# 터미널 2: Spring Boot 실행
cd demo-server/demo-auth
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**프로세스 실행 모드** — 터미널 1개로 실행:

```bash
cd demo-server/error-detector
npm run detect:auth         # demo-auth 실행 + 에러 감시
# 또는
npm run detect:system       # demo-system 실행 + 에러 감시
```

---

## 에러 감지 흐름

```
Spring Boot 로그 출력
        │
        ▼
  ┌─────────────┐
  │  LogParser   │  로그를 라인 단위로 파싱
  │              │  Exception/Error 패턴 감지
  │              │  멀티라인 스택 트레이스 조합
  └──────┬──────┘
         │ errorInfo
         ▼
  ┌─────────────────┐
  │ PatternAnalyzer  │  1차 분석: 알려진 에러 타입 매칭
  │  (패턴 매칭)     │  → 즉시 분석 결과 + 수정 제안 출력
  └──────┬──────────┘
         │ 매칭 실패 시
         ▼
  ┌─────────────────┐
  │  AiAnalyzer      │  2차 분석: Claude API 호출
  │  (Claude API)    │  → AI 분석 결과 추가 출력
  └──────┬──────────┘
         │
         ▼
  ┌─────────────┐
  │   Display    │  컬러 포맷으로 터미널에 출력
  └─────────────┘
```

---

## 지원하는 에러 패턴 (1차 패턴 매칭)

| 에러 타입 | 감지 내용 |
|-----------|-----------|
| NullPointerException | null 참조 감지, Java 14+ helpful NPE 메시지 파싱 |
| ArrayIndexOutOfBoundsException | 배열 인덱스 범위 초과, 유효 범위 표시 |
| IllegalArgumentException | 잘못된 인자 전달 감지 |
| NumberFormatException | 숫자 변환 실패, 원본 문자열 표시 |
| StackOverflowError | 무한 재귀 감지, 반복 호출 메서드 식별 |
| ClassCastException | 타입 캐스팅 실패, 소스/타겟 타입 표시 |
| BeanCreationException | Spring Bean 생성 오류, 의존성 문제 |
| PortInUseException | 포트 충돌, 프로세스 종료 명령어 제안 |
| ConnectException | 외부 서비스 연결 실패 (DB, Redis 등) |
| HttpMessageNotReadableException | 잘못된 JSON 요청 본문 |
| HttpRequestMethodNotSupportedException | HTTP 메서드 불일치 |
| NoHandlerFoundException | URL에 대응하는 컨트롤러 없음 |
| SQLSyntaxErrorException | SQL 구문 오류 |
| ApplicationStartupFailure | 애플리케이션 시작 실패 |

> 위 목록에 없는 에러는 AI 분석(2차)으로 자동 전환됩니다.

---

## 출력 예시

에러 발생 시 터미널에 아래와 같은 분석 블록이 표시됩니다:

```
════════════════════════════════════════════════════════════
  🔍 SPRING BOOT ERROR DETECTOR  [PATTERN MATCH]
════════════════════════════════════════════════════════════

  TYPE      : NullPointerException
  TIME      : 2026. 2. 11. 오후 11:30:00
  LEVEL     :  ERROR

  ──────────────────────────────────────────────────────────
  MESSAGE
  Cannot invoke "String.length()" because "str" is null

  ──────────────────────────────────────────────────────────
  ANALYSIS
  null 참조에 대해 메서드를 호출하거나 필드에 접근하려고 했습니다.
    변수 "str"이(가) null입니다.
    호출하려던 메서드: String.length()

  ──────────────────────────────────────────────────────────
  SUGGESTED FIX
  1. 해당 변수를 사용하기 전에 null 체크를 추가하세요
  2. Optional을 사용하세요
  3. Objects.requireNonNull()로 빠르게 실패하세요
  4. @NonNull 어노테이션을 사용하여 컴파일 타임에 감지하세요

  ──────────────────────────────────────────────────────────
  APP STACK TRACE
  at com.example.auth.service.UserService.getUser(UserService.java:42)

════════════════════════════════════════════════════════════
```

패턴 매칭 실패 + AI 분석 활성화 시 추가로 표시:

```
  ──────────────────────────────────────────────────────────
  🤖 AI ANALYSIS (Claude)
  ──────────────────────────────────────────────────────────

  ANALYSIS
  (Claude가 분석한 에러 원인 설명)

  AI SUGGESTED FIX
  (Claude가 제안하는 구체적인 수정 방법)

  ──────────────────────────────────────────────────────────
```

---

## npm 스크립트 목록

```bash
npm run setup          # 자동 설정 스크립트 (단일 모듈용, 참고용)
npm run start          # 도움말 출력
npm run watch:auth     # demo-auth 로그 파일 감시
npm run watch:system   # demo-system 로그 파일 감시
npm run detect:auth    # demo-auth 실행 + 에러 감시
npm run detect:system  # demo-system 실행 + 에러 감시
```

---

## 설정 파일 참조

### application-dev.yml (각 모듈)

로그 파일 출력을 위해 아래 설정이 추가되어 있습니다:

```yaml
logging:
  file:
    name: logs/demo-auth.log    # demo-auth 모듈
    # name: logs/demo-system.log  # demo-system 모듈
```

### .vscode/launch.json

```jsonc
{
    "configurations": [
        {
            "type": "java",
            "name": "demo-auth + Error Detector",
            "request": "launch",
            "mainClass": "com.example.auth.AuthApplication",
            "projectName": "demo-auth",
            "vmArgs": "-Dspring.profiles.active=dev",
            "preLaunchTask": "Error Detector: Watch demo-auth"
        }
        // ... 나머지 3개 구성
    ]
}
```

### .vscode/tasks.json

```jsonc
{
    "tasks": [
        {
            "label": "Error Detector: Watch demo-auth",
            "command": "node",
            "args": ["src/detector.js", "--watch", "../logs/demo-auth.log"],
            "options": { "cwd": "${workspaceFolder}/error-detector" },
            "isBackground": true
        }
        // ... demo-system 태스크
    ]
}
```

---

## 문제 해결

### Error Detector가 에러를 감지하지 못할 때

1. **로그 파일이 생성되는지 확인:**
   ```bash
   ls demo-server/logs/
   ```
   파일이 없다면 `application-dev.yml`의 `logging.file.name` 설정을 확인하세요.

2. **이전 로그 파일 삭제 후 재시작:**
   Error Detector는 시작 시 기존 로그를 모두 읽으므로, 이전 에러가 다시 표시될 수 있습니다.
   ```bash
   rm demo-server/logs/*.log
   ```

3. **프로필 확인:**
   `logging.file.name`이 `application-dev.yml`에 있으므로 dev 프로필이 활성화되어야 합니다. F5 구성에는 `-Dspring.profiles.active=dev`가 포함되어 있습니다.

### AI 분석이 동작하지 않을 때

1. `error-detector/.env` 파일에 `ANTHROPIC_API_KEY`가 올바르게 설정되어 있는지 확인
2. Error Detector 시작 시 `🤖 AI 분석: 활성화` 메시지가 표시되는지 확인
3. API 키가 유효하고 크레딧이 남아있는지 확인

### VSCode F5에서 Java 디버거가 시작되지 않을 때

1. **Extension Pack for Java** 확장이 설치되어 있는지 확인
2. JDK 17이 설치되고 `JAVA_HOME`이 설정되어 있는지 확인
3. VSCode가 프로젝트를 Java 프로젝트로 인식했는지 확인 (하단 상태바에서 Java 아이콘)
