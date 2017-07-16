#!/bin/sh

# The application can define additional startup commands in ./conf/bootrc and/or ./ext/bootrc.
if [ -e conf/bootrc ]; then
  source conf/bootrc
fi
if [ -e ext/bootrc ]; then
  source ext/bootrc
fi

CYAN='\033[0;36m'
NC='\033[0m'

# Determine the correct use of echo on this shell and platform (MacOS and Linux 'echo' differ when
# using /bin/sh).
echo_out=`echo -e`
if [ "${echo_out}" == "" ]; then
  echo_cmd="echo -e"
else
  echo_cmd="echo"
fi

echo 
$echo_cmd "${CYAN} ____  _    _     _       _     ____  ____  _   ${NC}"
$echo_cmd "${CYAN}| |_  | |  \ \_/ \ \    /| |_| | |_  | |_  | |  ${NC}"
$echo_cmd "${CYAN}|_|   |_|__ |_|   \_\/\/ |_| | |_|__ |_|__ |_|__${NC}"
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

  CMD="java $JVM_OPTS \
       -cp build/libs/flywheel-*jar \
       au.com.williamhill.flywheel.Launchpad \
       $@"
  echo "> Starting $CMD"
  $CMD
fi