#!/bin/sh
#
# Extracts the Flywheel version (semantic version + build number) and outputs it to stdout.
#

cd $(dirname "$0")/..

version_file=src/main/resources/flywheel.version
version=`cat $version_file`

build_file=src/main/resources/flywheel.build
if [ -f $build_file ]; then
  build=`cat $build_file`
else
  build=0
fi

echo ${version}_${build}
