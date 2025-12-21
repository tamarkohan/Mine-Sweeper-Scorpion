@echo off
echo Running QuestionCellUsedAfterAnswerTest...
echo.

cd /d "%~dp0"
mvn clean test -Dtest=QuestionCellUsedAfterAnswerTest

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================
    echo Test PASSED!
    echo ==========================================
) else (
    echo.
    echo ==========================================
    echo Test FAILED!
    echo ==========================================
)

pause

