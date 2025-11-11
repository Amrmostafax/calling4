#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass
# JVM options to this script.
DEFAULT_JVM_OPTS=()

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar


# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            # Use system's hard limit
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n "$MAX_FD"
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock;
# not all present processes (e.g. starting in headless mode) may be able to set
# this option.
if "$darwin"; then
    GRADLE_EXTRA_OPTS+=("-Xdock:name=$APP_NAME" "-Xdock:icon=$APP_HOME/media/gradle.icns")
fi

# For Cygwin, switch paths to Windows format before running java
if "$cygwin" ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`

    # We build the pattern for arguments to be converted to Windows paths
    ROOTDIRSRAW=`find -L / -maxdepth 1 -type d 2>/dev/null | grep -v /proc | grep -v /sys | grep -v /dev | grep -v /bin | grep -v /etc | grep -v /lib | grep -v /usr | grep -v /var | grep -v /tmp | grep -v /run | grep -v /sbin`
    if [ -n "$ROOTDIRSRAW" ] ; then
        ROOTDIRS=`echo $ROOTDIRSRAW | sed 's/ /|/g'`
        ARG_FMT="^($ROOTDIRS)"
    else
        ARG_FMT="^/"
    fi
    FIXARGS_PATTERN="^(--init-script|--settings-file|--build-file|--project-cache-dir|--project-dir|--gradle-user-home|--include-build|--system-prop|--daemon-base-dir|--embedded-script|--configure-on-demand|-I|-S|-b|-c|-p|-g|-D|.)*([\"']?)($ARG_FMT.*|.:.*)"
fi

# Escape application args
save () {
    for i do
        if "$cygwin" ; then
            i=`echo $i | sed -e "s#\"#'\"'#g" -e "s#'#'\"'\"'#g" -e "s#^$FIXARGS_PATTERN#\1\2`cygpath --path --mixed \"\3\"`#"`
        fi
        printf %s\\n "$i" | sed "s/'/'\\\\''/g;s/\"/\\\\\"/g;s/ /' ' /g"
    done
    printf \n
}
APP_ARGS_EXPANDED=`save "$@"`

# Collect all arguments for the java command, following the shell quoting and
# expansion rules.
#
# (must be vs. = to avoid empty string issues)
if [ "x$JAVA_OPTS" != "x" ] ; then
    eval "set -- $JAVA_OPTS"
    JAVA_OPTS=("$@")
fi
#
if [ "x$GRADLE_OPTS" != "x" ] ; then
    eval "set -- $GRADLE_OPTS"
    GRADLE_OPTS=("$@")
fi

# Add default JVM options.
for i in "${DEFAULT_JVM_OPTS[@]}"
do
    eval "set -- $i"
    DEFAULT_JVM_OPTS_EXPANDED=("$@")
    JAVA_OPTS+=("${DEFAULT_JVM_OPTS_EXPANDED[@]}")
done

# Split up the JVM options again just in case the default JVM options
# just assigned contained newlines.
eval "set -- ${JAVA_OPTS[*]}"
JAVA_OPTS=("$@")

# Add the jar file to the memory of arguments.
JAVA_OPTS+=("-Dorg.gradle.appname=$APP_BASE_NAME")

# Build the command line
eval "set -- \"$JAVACMD\" \"\${JAVA_OPTS[@]}\" -classpath \"$CLASSPATH\" org.gradle.wrapper.GradleWrapperMain \"\${GRADLE_OPTS[@]}\" $APP_ARGS_EXPANDED"

# Remove the quotes that we added earlier.
# This is required because eval is clumsy with embedded newlines.
COMMAND_STRING=("$@")
COMMAND_STRING="${COMMAND_STRING[@]}"

# Pass the command line to exec.
# Uses eval to handle embedded spaces correctly.
eval exec "$COMMAND_STRING"
