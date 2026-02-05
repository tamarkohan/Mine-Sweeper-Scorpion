@echo off
echo ==========================================
echo   Scorpion Minesweeper Launcher
echo ==========================================

REM 1. Look for the JAR file
if exist "target\Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar" (
    echo Launching Game...
    java -jar target\Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar
    goto :EOF
)

REM 2. If JAR is missing, check if they have Maven installed (For Developers)
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo JAR file not found. Building with Maven...
    call mvn package -DskipTests
    
    if exist "target\Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar" (
        echo Build Success! Launching...
        java -jar target\Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar
        goto :EOF
    )
)

REM 3. If no JAR and no Maven (For You or Recruiters)
echo.
echo [ERROR] Game file not found!
echo.
echo IF YOU ARE A DEVELOPER:
echo   Please open this project in your Java IDE (e.g., IntelliJ Idea or Eclipse) and run a Maven Build.
echo.
echo IF YOU ARE A USER:
echo   Please download the "Playable Demo" ZIP from the GitHub Releases page.
echo.
pause
