#!/bin/bash
# Spring Boot Error Detector 테스트 스크립트
# Spring Boot 서버가 실행 중일 때 사용하세요.
# 사용법: bash test-errors.sh

BASE_URL="http://localhost:8080"
DELAY=2

echo "=== Spring Boot Error Detector 테스트 ==="
echo ""

echo "[1/8] NullPointerException 테스트..."
curl -s "$BASE_URL/api/buggy/null"
echo ""
sleep $DELAY

echo "[2/8] ArrayIndexOutOfBoundsException 테스트..."
curl -s "$BASE_URL/api/buggy/array"
echo ""
sleep $DELAY

echo "[3/8] IllegalArgumentException 테스트..."
curl -s "$BASE_URL/api/buggy/illegal"
echo ""
sleep $DELAY

echo "[4/8] NumberFormatException 테스트..."
curl -s "$BASE_URL/api/buggy/number?value=abc"
echo ""
sleep $DELAY

echo "[5/8] StackOverflowError 테스트..."
curl -s "$BASE_URL/api/buggy/stackoverflow"
echo ""
sleep $DELAY

echo "[6/8] ClassCastException 테스트..."
curl -s "$BASE_URL/api/buggy/classcast"
echo ""
sleep $DELAY

echo "[7/8] ArithmeticException (0으로 나누기) 테스트..."
curl -s "$BASE_URL/api/buggy/divide"
echo ""
sleep $DELAY

echo "[8/8] HttpMessageNotReadableException (잘못된 JSON) 테스트..."
curl -s -X POST "$BASE_URL/api/buggy/badjson" -H "Content-Type: application/json" -d "{bad json"
echo ""

echo ""
echo "=== 테스트 완료 ==="
echo "Error Detector 터미널에서 분석 결과를 확인하세요."
