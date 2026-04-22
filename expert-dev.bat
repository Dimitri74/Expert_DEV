@echo off
setlocal

:: Define o caminho para a JRE portatil local
set "JAVA_EXE=.\jre8\bin\javaw.exe"

:: Verifica se a JRE portatil existe
if not exist "%JAVA_EXE%" (
    echo [ERRO] JRE portatil nao encontrada na pasta .\jre8
    echo Certifique-se de que a pasta 'jre8' esta no mesmo local que este arquivo.
    echo Tentando usar o Java do sistema como fallback...
    where javaw >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        start "" javaw -jar expert-dev-2.2.0.jar
        exit /b
    ) else (
        echo [ERRO] Java nao encontrado no sistema.
        pause
        exit /b
    )
)

:: Inicia o ExpertDev usando a JRE local
echo [INFO] Iniciando ExpertDev 2.2 com JRE Local...
start "" "%JAVA_EXE%" -jar expert-dev-2.2.0.jar

endlocal
