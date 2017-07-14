#!/bin/sh

CYAN='\033[0;36m'
NC='\033[0m'

echo 
echo -e "${CYAN} ___ _           _           _ ${NC}"
echo -e "${CYAN}|  _| |_ _ _ _ _| |_ ___ ___| |${NC}"
echo -e "${CYAN}|  _| | | | | | |   | -_| -_| |${NC}"
echo -e "${CYAN}|_| |_|_  |_____|_|_|___|___|_|${NC}"
echo -e "${CYAN}      |___|                    ${NC}"
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

  LOG_FILE=conf/log4j.properties
  if [ -e $LOG_FILE ]; then
    echo "> Using external Log4j file ${LOG_FILE}"
    LOG4J_OPTS=-Dlog4j.configuration=file:${LOG_FILE}
  else
    echo "> Using default Log4j configuration"
  fi
  
  CMD="java $JVM_OPTS \
       $LOG4J_OPTS \
       -cp flywheel-standalone-full-*jar \
       au.com.williamhill.flywheel.Launchpad \
       $@"
  echo "> Starting $CMD"
  $CMD
fi