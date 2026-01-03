#!/bin/bash
# ===================================
# MySQL Database Initialization (Linux/Mac)
# ===================================

echo "MySQL 데이터베이스 초기화를 시작합니다..."
echo ""

# MySQL 명령어 확인
if ! command -v mysql &> /dev/null; then
    echo "[오류] MySQL을 찾을 수 없습니다."
    echo "MySQL이 설치되어 있는지 확인하세요."
    exit 1
fi

echo "MySQL 서버에 연결합니다..."
echo "비밀번호를 입력하세요:"
echo ""

# MySQL 스크립트 실행
mysql -u root -p < init-mysql.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "==================================="
    echo "데이터베이스 초기화 완료!"
    echo "데이터베이스명: demo_db"
    echo "==================================="
else
    echo ""
    echo "[오류] 데이터베이스 초기화 실패!"
    echo "MySQL 서버가 실행 중인지 확인하세요."
    exit 1
fi
