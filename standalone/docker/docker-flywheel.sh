#!/bin/sh

if [ "$1" == "--cmd" ]; then
  if [ $# -eq 1 ]; then
    sh
  else
    shift
    sh -c "$*"
  fi
else
  java -cp flywheel-standalone-full-*jar au.com.williamhill.flywheel.beacon.RunBeaconEdge $@
fi