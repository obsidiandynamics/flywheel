#!/bin/bash -ex
#
# Installs Flywheel dependencies on AWS Linux and registers Flywheel to run as a daemon on startup.
# Upon completion, starts the Flywheel service.
# Must run as root.
#

# Install OpenJDK 8 and make it the default JDK
yum install java-1.8.0-openjdk-devel -y
alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java

# JMX requires that the hostname is aliased with localhost in /etc/hosts
hostname=`hostname`
host_lines=`cat /etc/hosts | grep $hostname | wc -l`
if [ $host_lines -eq 0 ]; then
  echo 127.0.0.1 $hostname >> /etc/hosts
fi

# Source env variables from defaults, if they exist
if [ -f /etc/default/flywheel-env ]; then
  . /etc/default/flywheel-env
fi

# Determine the user we should be installing for
if [ -z ${FLYWHEEL_USER} ]; then
  user=`echo $(logname)`
else
  user=$FLYWHEEL_USER
fi

# Install the init script and make it start by default
user_home=`eval echo "~$user"`
ln -s ${user_home}/opt/flywheel/standalone/scripts/init.d/flywheel /etc/init.d
chkconfig --add flywheel

# Start the service
service flywheel start