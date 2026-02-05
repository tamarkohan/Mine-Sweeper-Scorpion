@echo off
REM Force the console to use UTF-8 so error messages look right
chcp 65001 >nul

echo ==========================================
echo   Scorpion Minesweeper Launcher
echo ==========================================

REM ---------------------------------------------------------
REM CASE 1: The "Playable Demo" (User downloaded the ZIP)
REM ---------------------------------------------------------
if exist "Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar" (
    echo Launching Game...
    REM The -Dfile.encoding=UTF-8 flag fixes the scrambled text
    java -Dfile.encoding=UTF-8 -jar Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar
    goto :EOF
)

REM ---------------------------------------------------------
REM CASE 2: The Developer Mode (User cloned the Code)
REM ---------------------------------------------------------
REM Check if the JAR exists in the target folder
if exist "target\Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar" (
    echo Found compiled game. Launching...
    java -Dfile.encoding=UTF-8 -jar target\Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar
    goto :EOF
)

REM If JAR is missing, check if they have Maven installed
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo JAR file not found. Auto-building with Maven...
    call mvn package -DskipTests
    
    if exist "target\Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar" (
        echo Build Success! Launching...
        java -Dfile.encoding=UTF-8 -jar target\Mine_Sweeper_Scorpion-1.0-SNAPSHOT.jar
        goto :EOF
    )
)

REM ---------------------------------------------------------
REM CASE 3: Failure (No JAR, No Maven)
REM ---------------------------------------------------------
echo.
echo [ERROR] Game file not found!
echo.
echo IF YOU ARE A DEVELOPER:
echo   Please open this project in IntelliJ IDEA and run "Maven -> Package".
echo.
echo IF YOU ARE A USER:
echo   Please download the "Playable Demo" ZIP from the GitHub Releases page.
echo.
pause
