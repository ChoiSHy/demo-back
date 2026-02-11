/**
 * Spring Boot 로그 파싱 엔진
 * 멀티라인 스택 트레이스를 하나의 에러 블록으로 조합하고,
 * Exception/Error 패턴을 감지한다.
 */

// Exception/Error를 감지하는 정규식 패턴들
const EXCEPTION_LINE_PATTERN = /^(\S+\.)+\w*(Exception|Error|Failure):\s*(.*)/;
const CAUSED_BY_PATTERN = /^Caused by:\s*(\S+):\s*(.*)/;
const STACK_TRACE_PATTERN = /^\s+at\s+(.+)/;
const SPRING_ERROR_PATTERN = /^\d{4}-\d{2}-\d{2}.*\s(ERROR|WARN)\s.*---\s+\[.*\]\s+\S+\s*:\s*(.*)/;
const SPRING_EXCEPTION_IN_LOG = /^\d{4}-\d{2}-\d{2}.*\s(ERROR)\s.*(?:Exception|Error).*:\s*(.*)/;
const APPLICATION_FAILED_PATTERN = /APPLICATION FAILED TO START/;
const PORT_IN_USE_PATTERN = /Port\s+(\d+)\s+was already in use|Web server failed to start.*Port.*already in use/;
const BEAN_ERROR_PATTERN = /Error creating bean with name '(\w+)'/;

export class LogParser {
  constructor() {
    this.currentError = null;
    this.stackTraceLines = [];
    this.isCollectingStackTrace = false;
    this.onError = null; // callback: (errorInfo) => void
    this.lineBuffer = '';
    // 중복 방지: 최근 방출된 에러 정보
    this.lastEmittedType = null;
    this.lastEmittedMessage = null;
    this.lastEmittedTime = 0;
  }

  /**
   * 에러 감지 시 호출될 콜백 등록
   */
  setErrorCallback(callback) {
    this.onError = callback;
  }

  /**
   * 로그 데이터 청크를 처리 (스트림에서 들어오는 데이터)
   */
  processChunk(chunk) {
    const text = this.lineBuffer + chunk;
    const lines = text.split(/\r?\n/);
    // 마지막 줄은 아직 완성되지 않았을 수 있으므로 버퍼에 보관
    this.lineBuffer = lines.pop() || '';

    for (const line of lines) {
      this.processLine(line);
    }
  }

  /**
   * 스트림 종료 시 남은 버퍼 처리
   */
  flush() {
    if (this.lineBuffer) {
      this.processLine(this.lineBuffer);
      this.lineBuffer = '';
    }
    this._emitCurrentError();
  }

  /**
   * 한 줄의 로그를 처리
   */
  processLine(line) {
    // 스택 트레이스 라인인지 확인
    const stackMatch = line.match(STACK_TRACE_PATTERN);
    if (stackMatch && this.isCollectingStackTrace) {
      this.stackTraceLines.push(stackMatch[1]);
      return;
    }

    // Caused by 라인 감지
    const causedByMatch = line.match(CAUSED_BY_PATTERN);
    if (causedByMatch && this.isCollectingStackTrace) {
      // root cause를 업데이트
      this.currentError.rootCause = {
        type: this._extractSimpleName(causedByMatch[1]),
        fullType: causedByMatch[1],
        message: causedByMatch[2].trim()
      };
      this.stackTraceLines.push(`Caused by: ${causedByMatch[1]}: ${causedByMatch[2]}`);
      return;
    }

    // 스택 트레이스 수집 중인데 새로운 일반 라인이 오면 → 에러 블록 종료
    if (this.isCollectingStackTrace && !stackMatch && !causedByMatch) {
      this._emitCurrentError();
    }

    // 새로운 Exception/Error 라인 감지
    const exceptionMatch = line.match(EXCEPTION_LINE_PATTERN);
    if (exceptionMatch) {
      this._startNewError({
        type: this._extractSimpleName(exceptionMatch[0].split(':')[0]),
        fullType: exceptionMatch[0].split(':')[0].trim(),
        message: exceptionMatch[3]?.trim() || '',
        rawLine: line
      });
      return;
    }

    // Spring 로그 포맷 내 에러 감지
    const springErrorMatch = line.match(SPRING_ERROR_PATTERN);
    if (springErrorMatch) {
      const level = springErrorMatch[1];
      const message = springErrorMatch[2];

      // 에러 메시지 안에 Exception/Error가 있는지 확인
      const innerException = message.match(/(\w+(?:Exception|Error)):\s*(.*)/);
      if (innerException) {
        // Spring 로그에 붙는 접미사 제거: "] with root cause" 등
        const cleanMessage = (innerException[2] || '').replace(/\]?\s*with root cause\s*$/, '').trim();
        this._startNewError({
          type: innerException[1],
          fullType: innerException[1],
          message: cleanMessage,
          rawLine: line,
          level
        });
        return;
      }

      // APPLICATION FAILED TO START
      if (APPLICATION_FAILED_PATTERN.test(line)) {
        this._startNewError({
          type: 'ApplicationStartupFailure',
          fullType: 'org.springframework.boot.ApplicationStartupFailure',
          message: 'APPLICATION FAILED TO START',
          rawLine: line,
          level: 'ERROR'
        });
        return;
      }

      // Port in use
      const portMatch = line.match(PORT_IN_USE_PATTERN);
      if (portMatch) {
        this._startNewError({
          type: 'PortInUseException',
          fullType: 'org.springframework.boot.web.server.PortInUseException',
          message: `Port ${portMatch[1] || ''} was already in use`,
          rawLine: line,
          level: 'ERROR'
        });
        return;
      }

      // Bean creation error
      const beanMatch = line.match(BEAN_ERROR_PATTERN);
      if (beanMatch) {
        this._startNewError({
          type: 'BeanCreationException',
          fullType: 'org.springframework.beans.factory.BeanCreationException',
          message: `Error creating bean with name '${beanMatch[1]}'`,
          rawLine: line,
          level: 'ERROR'
        });
        return;
      }
    }
  }

  /**
   * 새 에러 수집 시작
   */
  _startNewError(errorInfo) {
    // 같은 타입의 에러가 연속으로 오면 병합 (Spring 로그 + 스택 트레이스 중복 방지)
    if (this.currentError && this.currentError.type === errorInfo.type
        && this.stackTraceLines.length === 0) {
      // 이전 에러는 스택 트레이스 없이 감지된 것이므로 버리고 새로 시작
      this.currentError = null;
      this.isCollectingStackTrace = false;
    }

    // 이전 에러가 있으면 먼저 방출
    this._emitCurrentError();

    this.currentError = {
      timestamp: new Date().toISOString(),
      type: errorInfo.type,
      fullType: errorInfo.fullType,
      message: errorInfo.message,
      rawLine: errorInfo.rawLine,
      level: errorInfo.level || 'ERROR',
      rootCause: null,
      stackTrace: [],
      appStackTrace: [] // com.example 패키지만 필터링
    };
    this.stackTraceLines = [];
    this.isCollectingStackTrace = true;
  }

  /**
   * 현재 수집 중인 에러를 방출
   */
  _emitCurrentError() {
    if (!this.currentError) return;

    this.currentError.stackTrace = [...this.stackTraceLines];
    this.currentError.appStackTrace = this.stackTraceLines
      .filter(line => !line.startsWith('Caused by:'))
      .filter(line => {
        return line.includes('com.example') ||
               line.includes('com.myapp') ||
               line.includes('com.mycompany');
      })
      .slice(0, 5);

    const errorToEmit = { ...this.currentError };

    // 중복 방지: 같은 타입+메시지가 2초 이내에 다시 나오면 스킵
    // 단, 스택 트레이스가 있는 것을 우선 (스택 트레이스 없는 것은 스킵)
    const now = Date.now();
    const isDuplicate = this.lastEmittedType === errorToEmit.type
      && this._normalizeMessage(this.lastEmittedMessage) === this._normalizeMessage(errorToEmit.message)
      && (now - this.lastEmittedTime) < 2000;

    if (isDuplicate) {
      // 중복이지만 현재 것이 스택 트레이스를 가지고 있으면 대체 출력하지 않고 스킵
      // (이전에 이미 출력된 것이 더 좋거나 같은 수준이므로)
      this.currentError = null;
      this.stackTraceLines = [];
      this.isCollectingStackTrace = false;
      return;
    }

    this.lastEmittedType = errorToEmit.type;
    this.lastEmittedMessage = errorToEmit.message;
    this.lastEmittedTime = now;

    if (this.onError) {
      this.onError(errorToEmit);
    }

    this.currentError = null;
    this.stackTraceLines = [];
    this.isCollectingStackTrace = false;
  }

  /**
   * 메시지 정규화 (비교용) - 끝에 "] with root cause" 같은 접미사 제거
   */
  _normalizeMessage(msg) {
    if (!msg) return '';
    return msg.replace(/\]?\s*with root cause\s*$/, '').trim();
  }

  /**
   * FQCN에서 단순 클래스명 추출
   * 예: "java.lang.NullPointerException" → "NullPointerException"
   */
  _extractSimpleName(fqcn) {
    if (!fqcn) return 'UnknownException';
    const parts = fqcn.trim().split('.');
    return parts[parts.length - 1] || fqcn;
  }
}
