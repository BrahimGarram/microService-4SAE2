@echo off
echo Starting Spring Boot Project Service...
echo.

REM Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not found in PATH!
    echo.
    echo Please add Maven to your PATH:
    echo 1. Add: C:\maven\apache-maven-3.9.12-bin\apache-maven-3.9.12\bin
    echo 2. Close and reopen this terminal
    echo.
    pause
    exit /b 1
)

echo Maven found. Starting Project Service on port 8082...
echo.
mvn spring-boot:run

pause
