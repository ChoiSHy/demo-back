/**
 * 패턴 매칭 기반 에러 분석기 (1차 분석)
 * 알려진 Exception 타입별 원인 분석 + 수정 제안을 제공한다.
 */

const ERROR_PATTERNS = {
  NullPointerException: {
    analyze(errorInfo) {
      const msg = errorInfo.message || '';
      // Java 14+ helpful NPE 메시지 파싱
      const methodMatch = msg.match(/Cannot invoke "(.+?)"/);
      const varMatch = msg.match(/because "(.+?)" is null/);
      const returnMatch = msg.match(/because the return value of "(.+?)" is null/);

      let analysis = 'null 참조에 대해 메서드를 호출하거나 필드에 접근하려고 했습니다.';
      if (varMatch) {
        analysis += `\n  변수 "${varMatch[1]}"이(가) null입니다.`;
      }
      if (methodMatch) {
        analysis += `\n  호출하려던 메서드: ${methodMatch[1]}`;
      }
      if (returnMatch) {
        analysis += `\n  "${returnMatch[1]}"의 반환값이 null입니다.`;
      }
      return analysis;
    },
    suggestFix(errorInfo) {
      return `1. 해당 변수를 사용하기 전에 null 체크를 추가하세요:
   if (obj != null) { obj.method(); }
2. Optional을 사용하세요:
   Optional.ofNullable(obj).ifPresent(o -> o.method());
3. Objects.requireNonNull()로 빠르게 실패하세요:
   Objects.requireNonNull(obj, "obj must not be null");
4. @NonNull 어노테이션을 사용하여 컴파일 타임에 감지하세요.`;
    }
  },

  ArrayIndexOutOfBoundsException: {
    analyze(errorInfo) {
      const msg = errorInfo.message || '';
      const indexMatch = msg.match(/Index (\d+) out of bounds for length (\d+)/);
      if (indexMatch) {
        return `배열 인덱스 범위 초과: 길이가 ${indexMatch[2]}인 배열에서 인덱스 ${indexMatch[1]}에 접근하려 했습니다.\n  유효한 인덱스 범위: 0 ~ ${parseInt(indexMatch[2]) - 1}`;
      }
      return '배열의 유효 범위를 벗어난 인덱스에 접근하려 했습니다.';
    },
    suggestFix() {
      return `1. 접근 전 인덱스 범위를 확인하세요:
   if (index >= 0 && index < array.length) { ... }
2. for-each 루프 사용을 고려하세요:
   for (var item : array) { ... }
3. List를 사용한다면 get() 대신 stream을 고려하세요.
4. 하드코딩된 인덱스 값이 있다면 배열 크기와 맞는지 확인하세요.`;
    }
  },

  IllegalArgumentException: {
    analyze(errorInfo) {
      return `잘못된 인자가 메서드에 전달되었습니다.\n  메시지: ${errorInfo.message || '(없음)'}`;
    },
    suggestFix(errorInfo) {
      return `1. 메서드 호출 전에 인자 값을 검증하세요.
2. API 문서에서 허용되는 인자 범위를 확인하세요.
3. @RequestParam, @PathVariable에 validation을 추가하세요.
4. 메서드 진입점에서 Preconditions 체크를 추가하세요:
   if (age < 0) throw new IllegalArgumentException("Age cannot be negative");`;
    }
  },

  NumberFormatException: {
    analyze(errorInfo) {
      const msg = errorInfo.message || '';
      const inputMatch = msg.match(/For input string: "(.+?)"/);
      if (inputMatch) {
        return `문자열 "${inputMatch[1]}"을(를) 숫자로 변환할 수 없습니다.\n  숫자가 아닌 문자가 포함되어 있습니다.`;
      }
      return '숫자로 변환할 수 없는 문자열을 파싱하려 했습니다.';
    },
    suggestFix() {
      return `1. 파싱 전에 문자열이 숫자인지 검증하세요:
   if (str.matches("-?\\\\d+")) { Integer.parseInt(str); }
2. try-catch로 감싸서 처리하세요:
   try { int n = Integer.parseInt(str); } catch (NumberFormatException e) { ... }
3. @RequestParam에 defaultValue를 설정하세요:
   @RequestParam(defaultValue = "0") int value
4. Spring Validation (@Min, @Max)을 사용하세요.`;
    }
  },

  StackOverflowError: {
    analyze(errorInfo) {
      // 스택 트레이스에서 반복되는 메서드 찾기
      const trace = errorInfo.stackTrace || [];
      const methodCounts = {};
      for (const line of trace.slice(0, 50)) {
        const method = line.replace(/\(.*\)/, '').trim();
        methodCounts[method] = (methodCounts[method] || 0) + 1;
      }
      const recursive = Object.entries(methodCounts)
        .filter(([, count]) => count > 3)
        .map(([method]) => method);

      if (recursive.length > 0) {
        return `무한 재귀 호출이 감지되었습니다.\n  반복 호출된 메서드: ${recursive.join(', ')}`;
      }
      return '스택 오버플로우 발생. 무한 재귀 호출 또는 너무 깊은 호출 스택이 원인일 수 있습니다.';
    },
    suggestFix() {
      return `1. 재귀 메서드에 종료 조건(base case)을 추가하세요.
2. 재귀를 반복문(iterative)으로 변환하세요.
3. 순환 참조가 있는지 데이터 구조를 확인하세요.
4. @JsonIgnore 등으로 Entity 간 순환 직렬화를 방지하세요.`;
    }
  },

  ClassCastException: {
    analyze(errorInfo) {
      const msg = errorInfo.message || '';
      const castMatch = msg.match(/class (.+?) cannot be cast to class (.+?)(?:\s|$)/);
      if (castMatch) {
        return `타입 캐스팅 실패: "${castMatch[1]}"을(를) "${castMatch[2]}"(으)로 변환할 수 없습니다.`;
      }
      return '호환되지 않는 타입으로 캐스팅하려 했습니다.';
    },
    suggestFix() {
      return `1. instanceof로 타입을 먼저 확인하세요:
   if (obj instanceof TargetType t) { t.method(); }
2. 제네릭(Generics)을 사용하여 raw type을 피하세요.
3. 데이터 흐름을 추적하여 예상과 다른 타입이 들어온 원인을 찾으세요.
4. JSON 역직렬화 설정을 확인하세요.`;
    }
  },

  BeanCreationException: {
    analyze(errorInfo) {
      const msg = errorInfo.message || '';
      const beanMatch = msg.match(/bean with name '(\w+)'/);
      let analysis = 'Spring Bean 생성 중 오류가 발생했습니다.';
      if (beanMatch) {
        analysis += `\n  문제 Bean: ${beanMatch[1]}`;
      }
      // root cause 확인
      if (errorInfo.rootCause) {
        analysis += `\n  근본 원인: ${errorInfo.rootCause.type} - ${errorInfo.rootCause.message}`;
      }
      return analysis;
    },
    suggestFix() {
      return `1. 해당 Bean의 의존성이 모두 주입 가능한지 확인하세요.
2. @Component, @Service 등 스캔 대상 어노테이션이 있는지 확인하세요.
3. @ComponentScan 범위에 해당 패키지가 포함되는지 확인하세요.
4. 생성자 파라미터의 타입과 일치하는 Bean이 존재하는지 확인하세요.
5. 순환 의존성이 있다면 @Lazy를 고려하세요.`;
    }
  },

  PortInUseException: {
    analyze(errorInfo) {
      const msg = errorInfo.message || '';
      const portMatch = msg.match(/Port\s+(\d+)/);
      const port = portMatch ? portMatch[1] : '(알 수 없음)';
      return `포트 ${port}이(가) 이미 사용 중입니다.\n  다른 프로세스가 해당 포트를 점유하고 있습니다.`;
    },
    suggestFix(errorInfo) {
      const msg = errorInfo.message || '';
      const portMatch = msg.match(/Port\s+(\d+)/);
      const port = portMatch ? portMatch[1] : '8080';
      return `1. 해당 포트를 사용하는 프로세스를 종료하세요:
   Windows: netstat -ano | findstr :${port} → taskkill /PID <PID> /F
   Linux/Mac: lsof -i :${port} → kill -9 <PID>
2. application.properties에서 다른 포트를 사용하세요:
   server.port=8081
3. 이전에 실행한 Spring Boot 인스턴스가 남아있지 않은지 확인하세요.`;
    }
  },

  ConnectException: {
    analyze(errorInfo) {
      return `외부 서비스 연결에 실패했습니다.\n  메시지: ${errorInfo.message || '(없음)'}\n  DB, Redis, 외부 API 등의 서비스가 실행 중인지 확인이 필요합니다.`;
    },
    suggestFix() {
      return `1. 연결 대상 서비스(DB, Redis 등)가 실행 중인지 확인하세요.
2. application.properties의 호스트/포트 설정을 확인하세요.
3. 방화벽이나 네트워크 설정을 확인하세요.
4. 타임아웃 설정을 늘려보세요.`;
    }
  },

  HttpMessageNotReadableException: {
    analyze(errorInfo) {
      return `요청 본문(Request Body)을 읽을 수 없습니다.\n  잘못된 JSON 형식이거나 타입이 일치하지 않습니다.\n  메시지: ${errorInfo.message || ''}`;
    },
    suggestFix() {
      return `1. 요청 JSON이 올바른 형식인지 확인하세요 (따옴표, 쉼표 등).
2. Content-Type 헤더가 application/json인지 확인하세요.
3. JSON 필드명이 DTO 클래스의 필드명과 일치하는지 확인하세요.
4. 숫자 필드에 문자열을 보내고 있지 않은지 확인하세요.`;
    }
  },

  HttpRequestMethodNotSupportedException: {
    analyze(errorInfo) {
      const msg = errorInfo.message || '';
      const methodMatch = msg.match(/Request method '(\w+)' is not supported/);
      if (methodMatch) {
        return `HTTP 메서드 '${methodMatch[1]}'은(는) 이 엔드포인트에서 지원하지 않습니다.`;
      }
      return '지원하지 않는 HTTP 메서드로 요청했습니다.';
    },
    suggestFix() {
      return `1. 요청 HTTP 메서드를 확인하세요 (GET/POST/PUT/DELETE).
2. Controller의 @GetMapping, @PostMapping 등 매핑 어노테이션을 확인하세요.
3. API 문서에서 올바른 HTTP 메서드를 확인하세요.`;
    }
  },

  NoHandlerFoundException: {
    analyze(errorInfo) {
      return `요청 URL에 대응하는 핸들러(Controller 메서드)를 찾을 수 없습니다.\n  메시지: ${errorInfo.message || ''}`;
    },
    suggestFix() {
      return `1. 요청 URL에 오타가 없는지 확인하세요.
2. Controller의 @RequestMapping 경로를 확인하세요.
3. @RestController 어노테이션이 있는지 확인하세요.
4. @ComponentScan 범위에 Controller 패키지가 포함되는지 확인하세요.`;
    }
  },

  SQLSyntaxErrorException: {
    analyze(errorInfo) {
      return `SQL 구문 오류가 발생했습니다.\n  메시지: ${errorInfo.message || ''}\n  SQL 쿼리 또는 JPA/Hibernate 매핑에 문제가 있을 수 있습니다.`;
    },
    suggestFix() {
      return `1. SQL 쿼리 구문을 직접 DB 클라이언트에서 실행해 보세요.
2. @Query 어노테이션의 JPQL/nativeQuery를 확인하세요.
3. Entity 클래스의 @Table, @Column 매핑이 올바른지 확인하세요.
4. spring.jpa.show-sql=true로 실행되는 SQL을 확인하세요.`;
    }
  },

  ApplicationStartupFailure: {
    analyze(errorInfo) {
      return 'Spring Boot 애플리케이션 시작에 실패했습니다.\n  아래 로그에서 root cause를 확인하세요.';
    },
    suggestFix() {
      return `1. 의존성 충돌이 없는지 확인하세요 (mvn dependency:tree).
2. application.properties 설정값을 확인하세요.
3. DB 연결 정보가 올바른지 확인하세요.
4. 필요한 외부 서비스(DB, Redis 등)가 실행 중인지 확인하세요.
5. Java 버전이 프로젝트 요구사항과 일치하는지 확인하세요.`;
    }
  }
};

// 일반적인 RuntimeException에 대한 기본 분석
const DEFAULT_ANALYZER = {
  analyze(errorInfo) {
    return `${errorInfo.type} 발생.\n  메시지: ${errorInfo.message || '(없음)'}`;
  },
  suggestFix(errorInfo) {
    return `이 에러에 대한 사전 정의된 분석이 없습니다.\n스택 트레이스를 확인하고, 발생 위치의 코드를 점검하세요.`;
  }
};

export class PatternAnalyzer {
  /**
   * 에러 정보를 분석하여 결과를 반환
   * @param {object} errorInfo - LogParser에서 생성된 에러 정보
   * @returns {{ analysis: string, suggestedFix: string, matched: boolean }}
   */
  analyze(errorInfo) {
    // root cause가 있으면 해당 타입으로도 매칭 시도
    const typesToTry = [errorInfo.type];
    if (errorInfo.rootCause) {
      typesToTry.push(errorInfo.rootCause.type);
    }

    for (const type of typesToTry) {
      const pattern = ERROR_PATTERNS[type];
      if (pattern) {
        const targetInfo = type === errorInfo.rootCause?.type
          ? { ...errorInfo, ...errorInfo.rootCause }
          : errorInfo;

        return {
          analysis: pattern.analyze(targetInfo),
          suggestedFix: pattern.suggestFix(targetInfo),
          matched: true
        };
      }
    }

    // 매칭되지 않은 경우 기본 분석
    return {
      analysis: DEFAULT_ANALYZER.analyze(errorInfo),
      suggestedFix: DEFAULT_ANALYZER.suggestFix(errorInfo),
      matched: false
    };
  }
}
