@echo off
REM Compile and run the Library Management System (Windows).
REM Usage:  run.bat

cd /d "%~dp0"

echo Compiling...
if not exist bin mkdir bin

dir /s /b src\*.java > sources.txt
javac -d bin --release 17 @sources.txt
del sources.txt

echo Starting application...
echo.
java -cp bin com.library.main.Main

pause
