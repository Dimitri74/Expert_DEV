@echo off
setlocal
echo.
echo === Expert Dev 1.5 - Com Paralelismo e PDF ===
echo.

if defined JAVA8_HOME (
  set "JAVA_HOME=%JAVA8_HOME%"
  set "PATH=%JAVA_HOME%\bin;%PATH%"
  echo [INFO] Usando JAVA8_HOME: %JAVA8_HOME%
) else (
  echo [WARN] JAVA8_HOME nao definido. Usando Java atual do sistema.
)

java -version

mvn clean compile exec:java -Dexec.mainClass="ExpertDev" -q

pause
