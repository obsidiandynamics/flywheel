#!/bin/sh

cd $(dirname "$0")/..

# The application can define additional startup commands in /etc/default/flywheel-env, ./conf/bootrc and/or ./ext/bootrc.
if [ -e conf/bootrc ]; then
  . conf/bootrc
fi
if [ -e ext/bootrc ]; then
  . ext/bootrc
fi
if [ -e /etc/default/flywheel-env ]; then
  . /etc/default/flywheel-env
fi

if [ -e '../gradlew' ]; then
  cd ..
  ./gradlew :flywheel-standalone:build -x test
  cd -
fi

cyan='\033[0;36m'
nc='\033[0m'

# Determine the correct use of echo on this shell and platform (macOS and Linux 'echo' differ when
# using /bin/sh).
echo_out=`echo -e`
if [ "${echo_out}" = "" ]; then
  echo_cmd="echo -e"
else
  echo_cmd="echo"
fi

echo 
$echo_cmd "${cyan} ____  _    _     _       _     ____  ____  _   ${nc}"
$echo_cmd "${cyan}| |_  | |  \ \_/ \ \    /| |_| | |_  | |_  | |  ${nc}"
$echo_cmd "${cyan}|_|   |_|__ |_|   \_\/\/ |_| | |_|__ |_|__ |_|__${nc}"
echo

ulimit -Sa

if [ "$1" = "--cmd" ]; then
  if [ $# -eq 1 ]; then
    sh
  else
    shift
    sh -c "$@"
  fi
else
  if [ "$1" = "--jvm-opts" ]; then
    if [ $# -lt 2 ]; then
      echo "> Usage --jvm-opts <jvm options>"
      exit 1
    fi
    flywheel_jvm_opts=$2
    echo "> JVM options: ${flywheel_jvm_opts}"
    shift
    shift
  fi

  cmd="java $flywheel_jvm_opts \
       -cp build/libs/flywheel-standalone-full-*jar \
       au.com.williamhill.flywheel.Launchpad \
       $@"
  echo "> Starting $cmd"
  $cmd
fi
