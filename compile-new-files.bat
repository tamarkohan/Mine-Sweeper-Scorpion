@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo Compiling new Observer pattern files...
javac -d target\classes -cp target\classes src\main\java\Model\GameObserver.java src\main\java\Model\GameStateData.java src\main\java\Model\GameSubject.java
if %ERRORLEVEL% EQU 0 (
    echo Successfully compiled new files!
    echo.
    echo Compiling updated files...
    javac -d target\classes -cp target\classes src\main\java\Controller\GameController.java src\main\java\View\GamePanel.java
    if %ERRORLEVEL% EQU 0 (
        echo All files compiled successfully!
        echo.
        echo Running application...
        java -cp "target/classes" View.MainFrame
    ) else (
        echo Compilation of updated files failed!
        pause
    )
) else (
    echo Compilation of new files failed!
    pause
)

