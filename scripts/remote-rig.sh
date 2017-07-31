#!/bin/sh
JVM_ARGS="-server -XX:-MaxFDLimit -XX:+TieredCompilation -XX:+UseNUMA -XX:+UseCondCardMark -XX:-UseBiasedLocking -Xms1G -Xmx2G -Xss1M -XX:+UseParallelGC"
./gradlew -x test testJar
java $JVM_ARGS -cp build/libs/flywheel-core-test-*.jar $@ au.com.williamhill.flywheel.rig.RemoteRigBenchmark
