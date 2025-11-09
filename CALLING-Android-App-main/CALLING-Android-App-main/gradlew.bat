@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass
@rem JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVACMD=java.exe
%JAVACMD% -version >NUL 2>&1
if %ERRORLEVEL% == 0 goto execute

echo ERROR: JAVA_HOME is not set and no 'java.exe' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto fail

:findJavaFromJavaHome
set JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVACMD%" goto execute

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto fail

:execute
@rem Setup the command line

set CLASSPATH=%~dp0gradle\wrapper\gradle-wrapper.jar

@rem Escape application args
set "GRADLE_APP_NAME=%~n0"
set "GRADLE_ARGS=%*"
call :escapeArgs %GRADLE_ARGS%
goto :endEscapeArgs
:escapeArgs
set "GRADLE_ARGS_ESCAPED="
:escapeArgsLoop
if "%1"=="" goto :escapeArgsEnd
set "ARG=%1"
shift
if defined ARG call set "ARG=%%ARG:"=\"%%"
if defined ARG set "ARG=%ARG:'='\''%"
if defined ARG set "GRADLE_ARGS_ESCAPED=%GRADLE_ARGS_ESCAPED% "%ARG%"
goto :escapeArgsLoop
:escapeArgsEnd
goto :EOF
:endEscapeArgs

"%JAVACMD%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%GRADLE_APP_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %GRADLE_ARGS_ESCAPED%

:fail
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
exit /b %ERRORLEVEL%
