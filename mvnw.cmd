@echo off
setlocal
pushd "%~dp0"
if not exist ".mvn\wrapper\maven-wrapper.jar" (
  echo Downloading official Apache Maven Wrapper...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar' -OutFile '.mvn/wrapper/maven-wrapper.jar'"
  if errorlevel 1 (
    echo ERROR: Unable to download Maven Wrapper. Check Internet access.
    popd
    exit /b 1
  )
)
java "-Dmaven.multiModuleProjectDirectory=%CD%" -cp ".mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
set "CODE=%ERRORLEVEL%"
popd
exit /b %CODE%
