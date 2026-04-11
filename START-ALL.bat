@echo off
echo ============================================
echo   Freelancify Platform - Start All Services
echo ============================================
echo.

echo [1/6] Starting Keycloak...
start "Keycloak" cmd /k "C:\keycloak\keycloak-24.0.5\bin\kc.bat start-dev --http-port=8180"
echo     Keycloak starting on http://localhost:8180
timeout /t 15 /nobreak >nul

echo [2/6] Starting Eureka (Service Discovery)...
start "Eureka" cmd /k "cd /d %~dp0Eureka && mvn spring-boot:run"
echo     Eureka starting on http://localhost:8761
timeout /t 20 /nobreak >nul

echo [3/6] Starting API Gateway...
start "API Gateway" cmd /k "cd /d %~dp0ApiGateway4sae2 && mvn spring-boot:run"
echo     API Gateway starting on http://localhost:8090
timeout /t 5 /nobreak >nul

echo [4/6] Starting User Service (backend)...
start "User Service" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"
echo     User Service starting on http://localhost:8081
timeout /t 5 /nobreak >nul

echo [5/6] Starting Project Service...
start "Project Service" cmd /k "cd /d %~dp0project-service && mvn spring-boot:run"
echo     Project Service starting on http://localhost:8082
timeout /t 5 /nobreak >nul

echo [6/6] Starting Complaints Service...
start "Complaints Service" cmd /k "cd /d %~dp0complaints-service && mvn spring-boot:run"
echo     Complaints Service starting on http://localhost:8089/freelancity
timeout /t 5 /nobreak >nul

echo [7/7] Starting Contract Service...
start "Contract Service" cmd /k "cd /d %~dp0contract-service && mvn spring-boot:run"
echo     Contract Service starting on http://localhost:8083
timeout /t 5 /nobreak >nul

echo [8/8] Starting Badge Service...
start "Badge Service" cmd /k "cd /d %~dp0badge-service && mvn spring-boot:run"
echo     Badge Service starting on http://localhost:8085
timeout /t 5 /nobreak >nul

echo.
echo ============================================
echo   All backend services launched!
echo.
echo   To start the frontend, run in a new terminal:
echo     cd frontend
echo     npm install  (first time only)
echo     npm start
echo.
echo   Contract Service at:  http://localhost:8083
echo   Keycloak admin at:   http://localhost:8180/admin
echo   Eureka dashboard at: http://localhost:8761
echo ============================================
pause
