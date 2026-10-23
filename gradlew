#!/bin/sh
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi
if [ -n "$GRADLE_OPTS" ] ; then
    GRADLE_OPTS="$GRADLE_OPTS -Xmx64m -Xms64m"
fi
exec "$JAVACMD" $GRADLE_OPTS \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
