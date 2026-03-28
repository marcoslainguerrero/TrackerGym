@echo off
REM Script para ejecutar TrackerGym
echo.
echo ===================================
echo   Iniciando TrackerGym
echo ===================================
echo.
echo Esperando que se inicie la aplicacion...
echo.
echo Accede a: http://localhost:8080/login
echo.
echo Credenciales por defecto:
echo - Usuario: entrenador_master
echo - Contraseña: admin123
echo.
echo O registrate como cliente nuevo en:
echo http://localhost:8080/register
echo.
echo Presiona Ctrl+C para detener la aplicacion
echo.

cd /d "%~dp0"
java -jar target\TrackerGym-0.0.1-SNAPSHOT.jar

pause
