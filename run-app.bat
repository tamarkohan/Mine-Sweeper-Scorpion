@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==========================================
echo Running Scorpion Minesweeper
echo ==========================================
echo.

REM Check if classes exist, if not compile
if not exist "target\classes\View\MainFrame.class" (
    echo MainFrame.class not found. Compiling...
    echo.
    
    REM Compile Model classes first
    echo Compiling Model classes...
    javac -d target\classes -cp target\classes -source 19 -target 19 src\main\java\Model\*.java src\main\java\Model\specialcell\*.java src\main\java\Model\specialcell\factory\*.java
    if %ERRORLEVEL% NEQ 0 (
        echo Model compilation failed!
        pause
        exit /b 1
    )
    
    REM Compile Controller
    echo Compiling Controller classes...
    javac -d target\classes -cp target\classes -source 19 -target 19 src\main\java\Controller\*.java
    if %ERRORLEVEL% NEQ 0 (
        echo Controller compilation failed!
        pause
        exit /b 1
    )
    
    REM Compile View
    echo Compiling View classes...
    javac -d target\classes -cp target\classes -source 19 -target 19 src\main\java\View\*.java
    if %ERRORLEVEL% NEQ 0 (
        echo View compilation failed!
        pause
        exit /b 1
    )
    
    echo Compilation successful!
    echo.
)

echo Starting application...
echo.
java -cp "target/classes" View.MainFrame

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application failed to start!
    echo Checking if MainFrame.class exists...
    if exist "target\classes\View\MainFrame.class" (
        echo MainFrame.class found but error occurred.
    ) else (
        echo MainFrame.class NOT found!
    )
    pause
)

