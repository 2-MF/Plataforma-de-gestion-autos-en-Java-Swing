@echo off
rem Ejecuta la aplicacion (compila antes si es necesario)
cd /d "%~dp0"
if not exist GestionAutos.jar call build.bat
where javaw >nul 2>nul
if %errorlevel%==0 (
    start "" javaw -jar GestionAutos.jar
) else (
    java -jar GestionAutos.jar
)
