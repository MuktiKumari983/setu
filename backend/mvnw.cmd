@echo off
REM ---------------------------------------------------------------------
REM mvnw.cmd - Minimal self-contained Maven wrapper for Windows.
REM Uses an installed `mvn` if available, otherwise downloads Apache Maven
REM into the user home and runs it. Inspired by the official wrapper.
REM ---------------------------------------------------------------------
setlocal

set MAVEN_VERSION=3.9.9
set MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip
set DIST_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%

where mvn >nul 2>nul
if %errorlevel%==0 (
  mvn %*
  exit /b %errorlevel%
)

if not exist "%DIST_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd" (
  echo [mvnw] mvn not found - downloading Apache Maven %MAVEN_VERSION%...
  if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
  powershell -NoProfile -Command "New-Item -ItemType Directory -Force -Path '%DIST_DIR%' | Out-Null; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%DIST_DIR%\maven.zip'; Expand-Archive -Path '%DIST_DIR%\maven.zip' -DestinationPath '%DIST_DIR%' -Force"
  if errorlevel 1 (
    echo [mvnw] Could not download Maven. Install Maven manually and add it to PATH.
    exit /b 1
  )
)

"%DIST_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd" %*
exit /b %errorlevel%