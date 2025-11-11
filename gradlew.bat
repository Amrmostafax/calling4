@if "%DEBUG%" == "" @echo off
setlocal
set DEFAULT_JVM_OPTS=
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_HOME=%DIRNAME%
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
if defined JAVA_HOME set JAVACMD="%JAVA_HOME%\bin\java.exe"
if not defined JAVACMD set JAVACMD=java.exe
"%JAVACMD%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%~n0" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
