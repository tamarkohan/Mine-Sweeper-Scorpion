@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==========================================
echo Fixing and Running Scorpion Minesweeper
echo ==========================================
echo.

REM Create target\classes if it doesn't exist
if not exist target\classes mkdir target\classes

REM Step 1: Compile Model classes (including new Observer pattern files)
echo [1/3] Compiling Model classes...
for %%f in (src\main\java\Model\*.java) do (
    javac -d target\classes -cp target\classes -source 19 -target 19 "%%f" 2>nul
)
for /r src\main\java\Model\specialcell %%f in (*.java) do (
    javac -d target\classes -cp target\classes -source 19 -target 19 "%%f" 2>nul
)
echo Model classes compiled.

REM Step 2: Compile Controller classes
echo [2/3] Compiling Controller classes...
for %%f in (src\main\java\Controller\*.java) do (
    javac -d target\classes -cp target\classes -source 19 -target 19 "%%f" 2>nul
)
echo Controller classes compiled.

REM Step 3: Compile View classes
echo [3/3] Compiling View classes...
for %%f in (src\main\java\View\*.java) do (
    javac -d target\classes -cp target\classes -source 19 -target 19 "%%f" 2>nul
)
echo View classes compiled.

echo.
echo ==========================================
echo Checking compilation...
echo ==========================================

if exist "target\classes\View\MainFrame.class" (
    echo ✓ MainFrame.class found!
    echo.
    echo ==========================================
    echo Starting application...
    echo ==========================================
    echo.
    java -cp "target/classes" View.MainFrame
) else (
    echo ✗ ERROR: MainFrame.class NOT found!
    echo.
    echo Attempting full compilation with error output...
    javac -d target\classes -cp target\classes -source 19 -target 19 src\main\java\Model\GameObserver.java src\main\java\Model\GameStateData.java src\main\java\Model\GameSubject.java 2>&1
    javac -d target\classes -cp target\classes -source 19 -target 19 src\main\java\Controller\GameController.java 2>&1
    javac -d target\classes -cp target\classes -source 19 -target 19 src\main\java\View\GamePanel.java src\main\java\View\MainFrame.java 2>&1
    
    if exist "target\classes\View\MainFrame.class" (
        echo.
        echo ✓ Compilation successful! Running...
        java -cp "target/classes" View.MainFrame
    ) else (
        echo.
        echo ✗ Compilation failed. Please check errors above.
        pause
    )
)

