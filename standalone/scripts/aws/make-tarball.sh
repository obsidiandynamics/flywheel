#!/bin/sh -e
#
# Creates a tarball for distributing Flywheel to AWS EC2 instances. The path and name
# of the resulting tarball is echoed to stdout.
#

cd $(dirname "$0")/../../..

version=`scripts/flywheel-version.sh`
file=flywheel-${version}.tgz
out_dir=standalone/build/tarball

mkdir -p ${out_dir}
tar -zcf ${out_dir}/${file} scripts/* standalone/conf/* standalone/scripts/* standalone/build/libs/* $@

echo ${out_dir}/${file}
