#!/bin/sh

if [ "$1" == "--cmd" ]; then
  if [ $# -eq 1 ]; then
    sh
  else
    shift
    sh -c "$*"
  fi
else
  java -Dlog4j.configuration=file:ext/conf/log4j.properties \
       -cp flywheel-standalone-full-*jar \
       au.com.williamhill.flywheel.beacon.RunBeaconEdge \
       $@
fi