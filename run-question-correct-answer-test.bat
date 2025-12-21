@echo off
cd /d "%~dp0"
echo ==========================================
echo Running QuestionCorrectAnswerTest
echo ==========================================
echo.

mvn clean test -Dtest=QuestionCorrectAnswerTest

echo.
echo ==========================================
echo Test completed!
echo ==========================================
pause

