#!/bin/sh -x
#
# Uninstalls Flywheel.
# Must run as root.
#

service flywheel stop
chkconfig --del flywheel
rm /etc/init.d/flywheel
rm /etc/default/flywheel-env
rm /var/log/flywheel*
