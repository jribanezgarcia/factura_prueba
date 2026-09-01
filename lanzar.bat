@echo off
setlocal
cd /d "%~dp0"

set "MAVEN=C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"
if not exist "%MAVEN%" set "MAVEN=C:\Users\usuario\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd"
if not exist "%MAVEN%" (
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo No se encontro Maven ni en la ruta cacheada ni en el PATH.
        pause
        exit /b 1
    )
    set "MAVEN=mvn"
)

call "%MAVEN%" -q clean javafx:run
if errorlevel 1 (
    echo.
    echo La aplicacion termino con errores.
    pause
)
endlocal
