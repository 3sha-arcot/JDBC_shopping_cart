@echo off
if not exist "out" mkdir "out"

echo Compiling Java files...
javac -d out -cp "lib\mysql-connector-j.jar;src" src\Main.java src\config\*.java src\model\*.java src\service\*.java src\util\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b %ERRORLEVEL%
)

echo Running application...
java -cp "lib\mysql-connector-j.jar;out" Main
