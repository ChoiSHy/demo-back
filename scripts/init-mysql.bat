@echo off
REM ===================================
REM MySQL Database Initialization (Windows)
REM ===================================

echo MySQL 데이터베이스 초기화를 시작합니다...
echo.

REM MySQL 설치 경로 확인 (일반적인 경로)
set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin
set MYSQL_PATH2=C:\Program Files\MySQL\MySQL Server 8.4\bin

REM MySQL이 PATH에 있는지 확인
where mysql >nul 2>nul
if %errorlevel% equ 0 (
    echo MySQL이 PATH에서 발견되었습니다.
    goto :run_mysql
)

REM MySQL Server 8.0 경로 확인
if exist "%MYSQL_PATH%\mysql.exe" (
    echo MySQL Server 8.0이 발견되었습니다.
    set "PATH=%MYSQL_PATH%;%PATH%"
    goto :run_mysql
)

REM MySQL Server 8.4 경로 확인
if exist "%MYSQL_PATH2%\mysql.exe" (
    echo MySQL Server 8.4가 발견되었습니다.
    set "PATH=%MYSQL_PATH2%;%PATH%"
    goto :run_mysql
)

echo.
echo [오류] MySQL을 찾을 수 없습니다.
echo MySQL이 설치되어 있는지 확인하세요.
echo 또는 MySQL 경로를 수동으로 지정하세요.
echo.
pause
exit /b 1

:run_mysql
echo.
echo MySQL 서버에 연결합니다...
echo 비밀번호를 입력하세요:
echo.

mysql -u root -p < init-mysql.sql

if %errorlevel% equ 0 (
    echo.
    echo ===================================
    echo 데이터베이스 초기화 완료!
    echo 데이터베이스명: demo_db
    echo ===================================
) else (
    echo.
    echo [오류] 데이터베이스 초기화 실패!
    echo MySQL 서버가 실행 중인지 확인하세요.
)

echo.
pause
