#!/usr/bin/env node

/**
 * Spring Boot Error Auto-Detector
 * 메인 진입점
 *
 * 사용법:
 *   node src/detector.js "./mvnw spring-boot:run"     # 프로세스 실행 + 모니터링
 *   node src/detector.js "mvnw.cmd spring-boot:run"   # Windows CMD
 *   node src/detector.js --watch app.log              # 로그 파일 모니터링
 */

import 'dotenv/config';
import { spawn } from 'child_process';
import { createReadStream, watchFile, existsSync } from 'fs';
import { createInterface } from 'readline';
import { LogParser } from './log-parser.js';
import { PatternAnalyzer } from './pattern-analyzer.js';
import { AiAnalyzer } from './ai-analyzer.js';
import { Display } from './display.js';

class ErrorDetector {
  constructor() {
    this.logParser = new LogParser();
    this.patternAnalyzer = new PatternAnalyzer();
    this.aiAnalyzer = new AiAnalyzer();
    this.display = new Display();
    this.errorCount = 0;

    // 에러 감지 시 콜백 등록
    this.logParser.setErrorCallback((errorInfo) => this.handleError(errorInfo));
  }

  /**
   * 에러 감지 시 분석 및 출력
   */
  async handleError(errorInfo) {
    this.errorCount++;

    // 1차: 패턴 매칭 분석
    const patternResult = this.patternAnalyzer.analyze(errorInfo);

    this.display.printErrorAnalysis({
      errorInfo,
      analysis: patternResult.analysis,
      suggestedFix: patternResult.suggestedFix,
      source: patternResult.matched ? 'PATTERN MATCH' : 'BASIC ANALYSIS'
    });

    // 2차: 패턴 매칭 실패 시 또는 추가 분석이 필요할 때 AI API 호출
    if (this.aiAnalyzer.isEnabled() && !patternResult.matched) {
      const aiResult = await this.aiAnalyzer.analyze(errorInfo);
      if (aiResult) {
        this.display.printAiAnalysis(aiResult);
      }
    }
  }

  /**
   * 명령어를 실행하고 출력을 모니터링
   */
  runCommand(command) {
    this.display.printBanner();
    this.display.printStatus({
      aiEnabled: this.aiAnalyzer.isEnabled(),
      command,
      watchMode: false
    });

    // 명령어를 분리
    const isWindows = process.platform === 'win32';
    const shell = isWindows ? true : false;
    const args = command.split(/\s+/);
    const cmd = args.shift();

    const child = spawn(cmd, args, {
      shell,
      cwd: process.cwd(),
      stdio: ['inherit', 'pipe', 'pipe']
    });

    // stdout 처리
    child.stdout.on('data', (data) => {
      const text = data.toString();
      // 원본 로그 그대로 출력
      process.stdout.write(data);
      // 파서에 전달
      this.logParser.processChunk(text);
    });

    // stderr 처리
    child.stderr.on('data', (data) => {
      const text = data.toString();
      // 원본 로그 그대로 출력
      process.stderr.write(data);
      // 파서에 전달 (Spring Boot는 에러를 stderr로도 출력)
      this.logParser.processChunk(text);
    });

    child.on('close', (code) => {
      this.logParser.flush();
      this.display.printSummary(this.errorCount);
      if (code !== 0) {
        console.log(`프로세스가 코드 ${code}로 종료되었습니다.`);
      }
    });

    child.on('error', (err) => {
      console.error(`명령어 실행 실패: ${err.message}`);
      console.error('명령어를 확인하세요:', command);
      process.exit(1);
    });

    // Ctrl+C 처리
    process.on('SIGINT', () => {
      child.kill('SIGINT');
      this.logParser.flush();
      this.display.printSummary(this.errorCount);
      process.exit(0);
    });

    // Windows에서 SIGBREAK 처리
    process.on('SIGBREAK', () => {
      child.kill();
      this.logParser.flush();
      this.display.printSummary(this.errorCount);
      process.exit(0);
    });
  }

  /**
   * 로그 파일을 모니터링 (tail -f 방식)
   */
  watchLogFile(filePath) {
    this.display.printBanner();
    this.display.printStatus({
      aiEnabled: this.aiAnalyzer.isEnabled(),
      command: filePath,
      watchMode: true
    });

    // 파일이 아직 없으면 생성될 때까지 대기
    if (!existsSync(filePath)) {
      console.log(`  로그 파일 대기 중: ${filePath}`);
      console.log('  Spring Boot가 시작되면 자동으로 모니터링을 시작합니다...\n');
      this._waitForFile(filePath, () => this._startWatching(filePath));
      return;
    }

    this._startWatching(filePath);
  }

  /**
   * 파일이 생성될 때까지 폴링으로 대기
   */
  _waitForFile(filePath, callback) {
    const check = setInterval(() => {
      if (existsSync(filePath)) {
        clearInterval(check);
        callback();
      }
    }, 500);
  }

  /**
   * 로그 파일 모니터링 시작
   */
  _startWatching(filePath) {
    console.log(`  로그 파일 감지됨. 모니터링 시작!\n`);

    // 먼저 기존 내용을 읽기
    const rl = createInterface({
      input: createReadStream(filePath, { encoding: 'utf-8' }),
      crlfDelay: Infinity
    });

    let lineCount = 0;
    rl.on('line', (line) => {
      lineCount++;
      this.logParser.processLine(line);
    });

    rl.on('close', () => {
      this.logParser.flush();
      console.log(`기존 로그 ${lineCount}줄 분석 완료. 새로운 로그를 모니터링합니다...\n`);

      // 파일 변경 감시
      let lastSize = 0;
      watchFile(filePath, { interval: 500 }, (curr, prev) => {
        if (curr.size > prev.size) {
          const stream = createReadStream(filePath, {
            start: prev.size,
            encoding: 'utf-8'
          });
          stream.on('data', (chunk) => {
            this.logParser.processChunk(chunk);
          });
          stream.on('end', () => {
            this.logParser.flush();
          });
        }
      });
    });

    // Ctrl+C 처리
    process.on('SIGINT', () => {
      this.logParser.flush();
      this.display.printSummary(this.errorCount);
      process.exit(0);
    });
  }
}

// --- CLI ---
function main() {
  const args = process.argv.slice(2);

  if (args.length === 0) {
    console.log(`
Spring Boot Error Auto-Detector

사용법:
  node src/detector.js "<spring-boot-command>"    프로세스 실행 + 에러 모니터링
  node src/detector.js --watch <logfile>          로그 파일 모니터링

예시:
  node src/detector.js "./mvnw spring-boot:run"
  node src/detector.js "mvnw.cmd spring-boot:run"
  node src/detector.js --watch app.log

환경 변수:
  ANTHROPIC_API_KEY    Claude API 키 (선택사항, AI 분석 활성화)
`);
    process.exit(0);
  }

  const detector = new ErrorDetector();

  if (args[0] === '--watch' && args[1]) {
    detector.watchLogFile(args[1]);
  } else {
    const command = args.join(' ');
    detector.runCommand(command);
  }
}

main();
