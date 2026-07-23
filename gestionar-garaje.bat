@echo off
setlocal
chcp 65001 >nul

set "MODE=%~1"
if "%MODE%"=="" set "MODE=gui"

if /I not "%MODE%"=="gui" if /I not "%MODE%"=="consola" goto :usage

echo [1/3] Preparando compilacion...
if not exist bin mkdir bin

> .sources.txt (
  for /R src %%F in (*.java) do echo %%F
)

echo [2/3] Compilando fuentes...
javac -encoding UTF-8 -d bin @.sources.txt
if errorlevel 1 (
  echo.
  echo Error: la compilacion ha fallado.
  exit /b 1
)

echo [3/3] Iniciando aplicacion en modo %MODE%...
if /I "%MODE%"=="consola" (
  java -cp bin garaje.AplicacionGestionGaraje --consola
) else (
  java -cp bin garaje.AplicacionGestionGaraje
)

exit /b %errorlevel%

:usage
echo Uso: gestionar-garaje.bat [gui^|consola]
echo.
echo Ejemplos:
echo   gestionar-garaje.bat
echo   gestionar-garaje.bat gui
echo   gestionar-garaje.bat consola
exit /b 2
