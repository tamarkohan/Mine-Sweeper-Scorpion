@echo off
cd /d "%~dp0"
mvn clean compile exec:java -Dexec.mainClass="View.MainFrame"
pause
