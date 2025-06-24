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

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

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

CLASSPATH_SEPARATOR=:
if $cygwin || $msys; then
  CLASSPATH_SEPARATOR=\;
fi

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
if ! $cygwin && ! $msys && ! $nonstop ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            # use the system max
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock;
# also pass a default JAVA_OPTS when running headless.
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if $cygwin || $msys; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`

    JAVACMD=`cygpath --unix "$JAVACMD"`

    # We build the pattern for arguments to be converted to Windows paths
    ROOTDIRSRAW=`find -L / -maxdepth 1 -mindepth 1 -type d 2>/dev/null`
    SEP=""
    for dir in $ROOTDIRSRAW ; do
        ROOTDIRS="$ROOTDIRS$SEP$dir"
        SEP="|"
    done
    PATTERNDIRS="^($ROOTDIRS)"
    WINPATTERNDIRS=`cygpath --windows --ignore "$PATTERNDIRS"`
    ERRORS="`echo "$WINPATTERNDIRS"|grep '^Error:'`"
    if [ -n "$ERRORS" ] ; then
        WINPATTERNDIRS="`echo "$WINPATTERNDIRS"|sed 's/^Error: //g'`"
        warn "$ERRORS"
    fi
    # A naive pattern to detect arguments that are not paths
    NONPATHPATTERN='^-D.*|^-[^-].*|^-X.*|^-XX.*|^-agentlib:.*|^-agentpath:.*|^-javaagent:.*|^-ea|^-da|^-esa|^-dsa|^-eval|^-d[0-9][0-9]$'
    # Store the arguments in a temporary array
    Args=("$@")
    # Iterate over arguments and convert paths to Windows style
    for ((i=0; i<${#Args[@]}; i++)); do
        arg="${Args[$i]}"
        if [[ ! $arg =~ $NONPATHPATTERN ]] && [[ $arg =~ $PATTERNDIRS ]] ; then
            Args[$i]="`cygpath --path --mixed \"$arg\"`"
        fi
    done
    # Set the arguments again
    set -- "${Args[@]}"
fi

# Collect all arguments for the java command, following the shell quoting and substitution rules
JAVA_ARGS=()
# Start with the Gradle options
if [ -n "$GRADLE_OPTS" ]; then
    # Add the GRADLE_OPTS, applying shell quoting and substitution
    eval "set -- $GRADLE_OPTS"
    for arg in "$@"; do
        JAVA_ARGS=("${JAVA_ARGS[@]}" "$arg")
    done
fi
# Then the JAVA_OPTS
if [ -n "$JAVA_OPTS" ]; then
    # Add the JAVA_OPTS, applying shell quoting and substitution
    eval "set -- $JAVA_OPTS"
    for arg in "$@"; do
        JAVA_ARGS=("${JAVA_ARGS[@]}" "$arg")
    done
fi
# Then the DEFAULT_JVM_OPTS
if [ -n "$DEFAULT_JVM_OPTS" ]; then
    # Add the DEFAULT_JVM_OPTS, applying shell quoting and substitution
    eval "set -- $DEFAULT_JVM_OPTS"
    for arg in "$@"; do
        JAVA_ARGS=("${JAVA_ARGS[@]}" "$arg")
    done
fi
# Add the main class as the last argument of the `java` command
JAVA_ARGS=("${JAVA_ARGS[@]}" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain)
# Add the application arguments
JAVA_ARGS=("${JAVA_ARGS[@]}" "$@")

# Start the wrapper
exec "$JAVACMD" "${JAVA_ARGS[@]}"
