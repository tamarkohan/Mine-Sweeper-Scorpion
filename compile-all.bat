@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==========================================
echo Compiling all Java files
echo ==========================================
echo.

if not exist target\classes mkdir target\classes

echo Step 1: Compiling Model classes...
javac -d target\classes -source 19 -target 19 -cp target\classes src\main\java\Model\*.java src\main\java\Model\specialcell\*.java src\main\java\Model\specialcell\factory\*.java
if %ERRORLEVEL% NEQ 0 (
    echo Model compilation failed!
    pause
    exit /b 1
)

echo Step 2: Compiling Controller classes...
javac -d target\classes -source 19 -target 19 -cp target\classes src\main\java\Controller\*.java
if %ERRORLEVEL% NEQ 0 (
    echo Controller compilation failed!
    pause
    exit /b 1
)

echo Step 3: Compiling View classes...
javac -d target\classes -source 19 -target 19 -cp target\classes src\main\java\View\*.java
if %ERRORLEVEL% NEQ 0 (
    echo View compilation failed!
    pause
    exit /b 1
)

echo.
echo ==========================================
echo Compilation successful!
echo ==========================================
echo.
echo Checking if MainFrame.class exists...
if exist target\classes\View\MainFrame.class (
    echo MainFrame.class found!
    echo.
    echo Starting application...
    java -cp "target/classes" View.MainFrame
) else (
    echo ERROR: MainFrame.class not found!
    echo.
    echo Listing View classes:
    dir target\classes\View\*.class /b
    pause
)

