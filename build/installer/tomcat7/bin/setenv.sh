# Tomcat memory settings
# -Xms<size> set initial Java heap size
# -Xmx<size> set maximum Java heap size
# -Xss<size> set java thread stack size
# -XX:MaxPermSize sets the java PermGen size
JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxPermSize=128m -javaagent:lib/lucee-inst.jar";   # memory settings

# additional JVM arguments can be added to the above line as needed, such as
# custom Garbage Collection arguments.

export JAVA_OPTS;
