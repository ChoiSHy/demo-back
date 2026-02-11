/**
 * 분석 결과 출력 포맷터
 * chalk로 컬러풀한 시각적 블록을 터미널에 출력한다.
 */

import chalk from 'chalk';

const SEPARATOR = '═'.repeat(60);
const THIN_SEP = '─'.repeat(60);

// 에러 심각도별 색상
const SEVERITY = {
  CRITICAL: chalk.bgRed.white.bold,
  ERROR: chalk.red.bold,
  WARNING: chalk.yellow.bold,
  INFO: chalk.cyan.bold
};

// 심각한 에러 타입
const CRITICAL_ERRORS = new Set([
  'StackOverflowError', 'OutOfMemoryError',
  'ApplicationStartupFailure', 'PortInUseException'
]);

export class Display {
  /**
   * 에러 분석 결과를 시각적 블록으로 출력
   */
  printErrorAnalysis({ errorInfo, analysis, suggestedFix, source }) {
    const severity = CRITICAL_ERRORS.has(errorInfo.type) ? 'CRITICAL' : 'ERROR';
    const severityLabel = SEVERITY[severity];
    const timestamp = new Date().toLocaleString('ko-KR');

    const output = [
      '',
      chalk.red(SEPARATOR),
      severityLabel(`  🔍 SPRING BOOT ERROR DETECTOR  [${source}]  `),
      chalk.red(SEPARATOR),
      '',
      `  ${chalk.white.bold('TYPE')}      : ${chalk.red.bold(errorInfo.type)}`,
      `  ${chalk.white.bold('TIME')}      : ${chalk.gray(timestamp)}`,
      `  ${chalk.white.bold('LEVEL')}     : ${severityLabel(` ${severity} `)}`,
    ];

    if (errorInfo.message) {
      output.push(
        '',
        chalk.gray(THIN_SEP),
        `  ${chalk.white.bold('MESSAGE')}`,
        `  ${chalk.yellow(errorInfo.message)}`,
      );
    }

    output.push(
      '',
      chalk.gray(THIN_SEP),
      `  ${chalk.white.bold('ANALYSIS')}`,
      ...analysis.split('\n').map(line => `  ${chalk.cyan(line)}`),
    );

    if (suggestedFix) {
      output.push(
        '',
        chalk.gray(THIN_SEP),
        `  ${chalk.white.bold('SUGGESTED FIX')}`,
        ...suggestedFix.split('\n').map(line => `  ${chalk.green(line)}`),
      );
    }

    if (errorInfo.appStackTrace?.length > 0) {
      output.push(
        '',
        chalk.gray(THIN_SEP),
        `  ${chalk.white.bold('APP STACK TRACE')}`,
        ...errorInfo.appStackTrace.map(line => `  ${chalk.gray('at ' + line)}`),
      );
    }

    output.push(
      '',
      chalk.red(SEPARATOR),
      ''
    );

    console.log(output.join('\n'));
  }

  /**
   * AI 분석 결과를 추가로 출력
   */
  printAiAnalysis({ analysis, suggestedFix }) {
    const output = [
      '',
      chalk.magenta(THIN_SEP),
      chalk.magenta.bold('  🤖 AI ANALYSIS (Claude)'),
      chalk.magenta(THIN_SEP),
      '',
      `  ${chalk.white.bold('ANALYSIS')}`,
      ...analysis.split('\n').map(line => `  ${chalk.cyan(line)}`),
    ];

    if (suggestedFix) {
      output.push(
        '',
        `  ${chalk.white.bold('AI SUGGESTED FIX')}`,
        ...suggestedFix.split('\n').map(line => `  ${chalk.green(line)}`),
      );
    }

    output.push(
      '',
      chalk.magenta(THIN_SEP),
      ''
    );

    console.log(output.join('\n'));
  }

  /**
   * 검출기 시작 배너 출력
   */
  printBanner() {
    console.log('');
    console.log(chalk.cyan(SEPARATOR));
    console.log(chalk.cyan.bold('  Spring Boot Error Auto-Detector v1.0'));
    console.log(chalk.gray('  런타임 에러를 자동으로 감지하고 분석합니다.'));
    console.log(chalk.cyan(SEPARATOR));
    console.log('');
  }

  /**
   * 설정 상태 출력
   */
  printStatus({ aiEnabled, command, watchMode }) {
    if (watchMode) {
      console.log(chalk.gray(`  📁 로그 파일 모니터링: ${command}`));
    } else {
      console.log(chalk.gray(`  🚀 실행 명령어: ${command}`));
    }
    console.log(chalk.gray(`  🤖 AI 분석: ${aiEnabled ? chalk.green('활성화') : chalk.yellow('비활성화 (ANTHROPIC_API_KEY 미설정)')}`));
    console.log(chalk.gray(`  ⏱️  시작 시간: ${new Date().toLocaleString('ko-KR')}`));
    console.log('');
  }

  /**
   * 에러 통계 출력
   */
  printSummary(errorCount) {
    console.log('');
    console.log(chalk.cyan(THIN_SEP));
    if (errorCount === 0) {
      console.log(chalk.green.bold('  ✅ 감지된 에러 없음'));
    } else {
      console.log(chalk.red.bold(`  ⚠️  총 ${errorCount}개의 에러가 감지되었습니다.`));
    }
    console.log(chalk.cyan(THIN_SEP));
    console.log('');
  }
}
