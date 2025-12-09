@echo off
echo ==========================================
echo Running All Tests
echo ==========================================
echo.

REM Compile all source files including tests
echo Compiling...
javac -d target/classes -cp "target/classes" -source 1.8 -target 1.8 src/main/java/View/*.java src/main/java/Controller/*.java src/main/java/Model/*.java src/test/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo ==========================================
echo Running Random Placement Test
echo ==========================================
java -cp "target/classes" RandomPlacementTest

echo.
echo ==========================================
echo Running Used Cell Test
echo ==========================================
java -cp "target/classes" UsedCellTest

echo.
echo ==========================================
echo Running Minesweeper Logic Test
echo ==========================================
java -cp "target/classes" MinesweeperLogicTest

echo.
echo ==========================================
echo All tests completed!
echo ==========================================
pause

