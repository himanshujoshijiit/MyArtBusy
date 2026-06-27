@echo off
echo ========================================
echo   MakeupSeven - Launch Platform
echo ========================================
echo.

where docker >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Docker is not installed or not in PATH.
    echo Install Docker Desktop: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)

docker info >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Docker Desktop is not running.
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)

if not exist .env (
    echo Creating .env from .env.example...
    copy .env.example .env
)

echo Building and starting all services...
echo This takes 3-5 minutes on first run.
echo.

docker compose up --build -d

echo.
echo ========================================
echo   MakeupSeven is starting!
echo ========================================
echo.
echo   App:        http://localhost:3000
echo   Java API:   http://localhost:8080
echo   Search API: http://localhost:8000/docs
echo.
echo   Demo client: demo@makeupseven.com / demo123
echo   Demo artist: priya@makeupseven.com / artist123
echo.
echo Run: docker compose logs -f
echo Stop: docker compose down
echo.
pause
