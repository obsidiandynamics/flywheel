#!/bin/bash -e
#
# Installs Flywheel dependencies on AWS Linux and registers Flywheel to run as a daemon on startup.
# Upon completion, starts the Flywheel service.
# Must run as root.
#

yum install java-1.8.0-openjdk-devel -y
alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java
echo 127.0.0.1 `hostname` >> append /etc/hosts
ln -s /home/ec2-user/opt/flywheel/wha/scripts/flywheel /etc/init.d
chkconfig --add flywheel
service flywheel start
