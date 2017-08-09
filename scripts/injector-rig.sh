#!/bin/sh
JVM_ARGS="-XX:-MaxFDLimit -XX:+TieredCompilation -XX:+UseNUMA -XX:+UseCondCardMark -XX:-UseBiasedLocking \
          -Xms1G -Xmx2G -Xss1M -XX:MaxDirectMemorySize=3G \
          -XX:+DisableExplicitGC -Xloggc:log/injector-gc-%t.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
          -XX:+UseParallelGC"
./gradlew -x test testJar
mkdir -p log
java $JVM_ARGS -cp build/libs/flywheel-core-test-*.jar $@ au.com.williamhill.flywheel.rig.InjectorRigBenchmark
