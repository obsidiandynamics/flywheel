#!/bin/sh -e
#
# Creates a tarball for distributing Flywheel to AWS EC2 instances.
#

cd $(dirname "$0")/../../..

VERSION=`cat src/main/resources/flywheel.version`
BUILD=`cat src/main/resources/flywheel.build`
FILE=flywheel-${VERSION}_${BUILD}.tgz
OUT_DIR=standalone/build/tarball

mkdir -p ${OUT_DIR}
tar -zcf ${OUT_DIR}/${FILE} standalone/conf/* standalone/scripts/* standalone/build/libs/* $@

echo ${OUT_DIR}/${FILE}
