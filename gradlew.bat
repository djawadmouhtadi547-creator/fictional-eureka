@echo off
setlocal
set "GRADLE_VERSION=8.6"
if not defined GRADLE_USER_HOME set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "DIST_ROOT=%GRADLE_USER_HOME%\wrapper\dists\darknull-bootstrap-%GRADLE_VERSION%"
set "DIST_HOME=%DIST_ROOT%\gradle-%GRADLE_VERSION%"
set "GRADLE_BIN=%DIST_HOME%\bin\gradle.bat"
if not exist "%GRADLE_BIN%" (
  if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"
  echo DARKNULL-06: downloading Gradle %GRADLE_VERSION% (first run only)
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip'; Expand-Archive -Force '%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip' '%DIST_ROOT%'"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%
