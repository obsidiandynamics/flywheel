#!/bin/sh

echo 
echo " ___ _           _           _ "
echo "|  _| |_ _ _ _ _| |_ ___ ___| |"
echo "|  _| | | | | | |   | -_| -_| |"
echo "|_| |_|_  |_____|_|_|___|___|_|"
echo "      |___|                    "
echo 

if [ "$1" == "--cmd" ]; then
  if [ $# -eq 1 ]; then
    sh
  else
    shift
    sh -c "$@"
  fi
else
  if [ "$1" == "--jvm-opts" ]; then
    if [ $# -lt 2 ]; then
      echo "> Usage --jvm-opts <jvm options>"
      exit 1
    fi
    JVM_OPTS=$2
    echo "> JVM options: ${JVM_OPTS}"
    shift
    shift
  fi

  LOG_FILE=ext/conf/log4j.properties
  if [ -e $LOG_FILE ]; then
    echo "> Using external Log4j file ${LOG_FILE}"
    LOG4J_OPTS=-Dlog4j.configuration=file:${LOG_FILE}
  else
    echo "> Using default Log4j configuration"
  fi
  
  CMD="java $JVM_OPTS \
       $LOG4J_OPTS \
       -cp flywheel-standalone-full-*jar \
       au.com.williamhill.flywheel.beacon.RunBeaconEdge \
       $@"
  echo "> Starting $CMD"
  $CMD
fi