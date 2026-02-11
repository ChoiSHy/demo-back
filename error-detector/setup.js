#!/usr/bin/env node

/**
 * Error Detector 자동 설정 스크립트
 *
 * 어떤 Spring Boot 프로젝트에든 error-detector/ 폴더를 넣고
 * npm run setup 하면 자동으로:
 *   1. application.properties에 로그 파일 설정 추가
 *   2. .vscode/tasks.json 생성/병합
 *   3. .vscode/launch.json에 Error Detector 연동 설정 추가
 *   4. logs/ 디렉토리 생성
 *   5. .gitignore에 관련 항목 추가
 */

import { readFileSync, writeFileSync, existsSync, mkdirSync } from 'fs';
import { join, resolve } from 'path';

const ROOT = resolve(process.cwd(), '..');
const VSCODE_DIR = join(ROOT, '.vscode');
const LOG_DIR = join(ROOT, 'logs');

console.log('\n🔧 Spring Boot Error Detector 설정을 시작합니다...\n');
console.log(`  프로젝트 루트: ${ROOT}\n`);

// ─── 1. application.properties ───
setupApplicationProperties();

// ─── 2. logs/ 디렉토리 ───
setupLogsDir();

// ─── 3. .vscode/tasks.json ───
setupTasksJson();

// ─── 4. .vscode/launch.json ───
setupLaunchJson();

// ─── 5. .gitignore ───
setupGitignore();

console.log('\n✅ 설정 완료!\n');
console.log('  사용법:');
console.log('    1. VSCode에서 F5 → "Spring Boot + Error Detector" 선택');
console.log('    2. 또는 터미널에서: cd error-detector && npm run watch\n');


// ──────────────────────────────────────────────

function setupApplicationProperties() {
  // Maven/Gradle 프로젝트의 application.properties 찾기
  const candidates = [
    join(ROOT, 'src', 'main', 'resources', 'application.properties'),
    join(ROOT, 'src', 'main', 'resources', 'application.yml'),
  ];

  const propsFile = candidates.find(f => existsSync(f));

  if (!propsFile) {
    console.log('  ⚠️  application.properties를 찾을 수 없습니다.');
    console.log('     수동으로 추가하세요: logging.file.name=logs/spring-boot.log');
    return;
  }

  const content = readFileSync(propsFile, 'utf-8');
  const LINE = 'logging.file.name=logs/spring-boot.log';

  if (content.includes('logging.file.name')) {
    console.log('  ✓ application.properties: 로그 파일 설정이 이미 존재합니다.');
    return;
  }

  if (propsFile.endsWith('.yml')) {
    console.log('  ⚠️  application.yml 감지. 수동으로 추가하세요:');
    console.log('     logging:');
    console.log('       file:');
    console.log('         name: logs/spring-boot.log');
    return;
  }

  const newContent = content.trimEnd() + '\n\n# Error Detector용 로그 파일 출력\n' + LINE + '\n';
  writeFileSync(propsFile, newContent, 'utf-8');
  console.log('  ✓ application.properties: 로그 파일 설정 추가 완료');
}

function setupLogsDir() {
  if (!existsSync(LOG_DIR)) {
    mkdirSync(LOG_DIR, { recursive: true });
    console.log('  ✓ logs/ 디렉토리 생성 완료');
  } else {
    console.log('  ✓ logs/ 디렉토리: 이미 존재합니다.');
  }
}

function setupTasksJson() {
  if (!existsSync(VSCODE_DIR)) {
    mkdirSync(VSCODE_DIR, { recursive: true });
  }

  const tasksFile = join(VSCODE_DIR, 'tasks.json');

  const errorDetectorTask = {
    label: 'Start Error Detector',
    type: 'shell',
    command: 'node',
    args: ['src/detector.js', '--watch', '../logs/spring-boot.log'],
    options: { cwd: '${workspaceFolder}/error-detector' },
    isBackground: true,
    problemMatcher: [],
    presentation: {
      reveal: 'always',
      panel: 'dedicated',
      group: 'errorDetector'
    }
  };

  const installTask = {
    label: 'Install Error Detector',
    type: 'shell',
    command: 'npm',
    args: ['install'],
    options: { cwd: '${workspaceFolder}/error-detector' }
  };

  if (existsSync(tasksFile)) {
    // 기존 tasks.json에 병합
    const existing = JSON.parse(readFileSync(tasksFile, 'utf-8'));
    const tasks = existing.tasks || [];

    const hasDetector = tasks.some(t => t.label === 'Start Error Detector');
    if (hasDetector) {
      console.log('  ✓ tasks.json: Error Detector 태스크가 이미 존재합니다.');
      return;
    }

    tasks.push(errorDetectorTask, installTask);
    existing.tasks = tasks;
    writeFileSync(tasksFile, JSON.stringify(existing, null, 4) + '\n', 'utf-8');
    console.log('  ✓ tasks.json: Error Detector 태스크 추가 완료');
  } else {
    // 새로 생성
    const tasksJson = {
      version: '2.0.0',
      tasks: [errorDetectorTask, installTask]
    };
    writeFileSync(tasksFile, JSON.stringify(tasksJson, null, 4) + '\n', 'utf-8');
    console.log('  ✓ tasks.json: 새로 생성 완료');
  }
}

function setupLaunchJson() {
  const launchFile = join(VSCODE_DIR, 'launch.json');

  if (!existsSync(launchFile)) {
    console.log('  ⚠️  launch.json이 없습니다. Spring Boot 디버그 설정을 먼저 생성하세요.');
    console.log('     생성 후 preLaunchTask: "Start Error Detector" 를 추가하면 됩니다.');
    return;
  }

  const content = readFileSync(launchFile, 'utf-8');

  if (content.includes('Start Error Detector')) {
    console.log('  ✓ launch.json: Error Detector 연동이 이미 존재합니다.');
    return;
  }

  const launch = JSON.parse(content);
  const configs = launch.configurations || [];

  // 기존 Java/Spring Boot 설정을 찾아서 복제 + preLaunchTask 추가
  const javaConfig = configs.find(c => c.type === 'java');

  if (javaConfig) {
    const detectorConfig = {
      ...javaConfig,
      name: javaConfig.name.replace(/<.*>/, '').trim() + ' + Error Detector',
      preLaunchTask: 'Start Error Detector'
    };

    // 원래 설정 이름에 "(단독)" 추가
    if (!javaConfig.name.includes('단독')) {
      javaConfig.name = javaConfig.name.replace(/<.*>/, '').trim() + ' (단독)';
    }

    // Error Detector 버전을 맨 앞에 배치 (기본 선택되도록)
    configs.unshift(detectorConfig);
    launch.configurations = configs;

    writeFileSync(launchFile, JSON.stringify(launch, null, 4) + '\n', 'utf-8');
    console.log('  ✓ launch.json: "' + detectorConfig.name + '" 설정 추가 완료');
  } else {
    console.log('  ⚠️  launch.json에 Java 설정을 찾을 수 없습니다.');
    console.log('     수동으로 preLaunchTask: "Start Error Detector" 를 추가하세요.');
  }
}

function setupGitignore() {
  const gitignoreFile = join(ROOT, '.gitignore');
  const entries = ['logs/', 'error-detector/node_modules/', 'error-detector/.env'];

  if (!existsSync(gitignoreFile)) {
    writeFileSync(gitignoreFile, entries.join('\n') + '\n', 'utf-8');
    console.log('  ✓ .gitignore: 새로 생성 완료');
    return;
  }

  const content = readFileSync(gitignoreFile, 'utf-8');
  const toAdd = entries.filter(e => !content.includes(e));

  if (toAdd.length === 0) {
    console.log('  ✓ .gitignore: 관련 항목이 이미 존재합니다.');
    return;
  }

  const newContent = content.trimEnd() + '\n\n# Error Detector\n' + toAdd.join('\n') + '\n';
  writeFileSync(gitignoreFile, newContent, 'utf-8');
  console.log('  ✓ .gitignore: ' + toAdd.join(', ') + ' 추가 완료');
}
