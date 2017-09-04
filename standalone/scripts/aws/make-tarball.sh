#!/bin/sh -e
#
# Creates a tarball for distributing Flywheel to AWS EC2 instances. The path and name
# of the resulting tarball is echoed to stdout. A gradle build is done before packing
# the tarball.
#

cd $(dirname "$0")/../../..

if [ -e 'gradlew' ]; then
  ./gradlew flywheel-standalone:build -x test
fi

version=`scripts/flywheel-version.sh`
file=flywheel-${version}.tgz
out_dir=standalone/build/tarball

mkdir -p ${out_dir}
tar -zcf ${out_dir}/${file} scripts/* standalone/conf/* standalone/scripts/* standalone/build/libs/* $@

echo ${out_dir}/${file}
