@echo off
cd /d "%~dp0"
echo ==========================================
echo Running QuestionManagerTest
echo ==========================================
echo.

mvn clean test -Dtest=QuestionManagerTest

echo.
echo ==========================================
echo Test completed!
echo ==========================================
pause

