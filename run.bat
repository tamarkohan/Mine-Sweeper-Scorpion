@echo off
echo ==========================================
echo Running Scorpion Minesweeper
echo ==========================================
echo.

REM Compile all source files
echo Compiling...
javac -d target/classes -cp "target/classes" -source 1.8 -target 1.8 src/main/java/View/*.java src/main/java/Controller/*.java src/main/java/Model/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Starting application...
echo.

REM Run the main application
java -cp "target/classes" View.MainFrame

pause

