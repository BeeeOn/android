#!/bin/bash
#------------------------------------------------------------------------------
#
#
#
#
# 
#------------------------------------------------------------------------------


PACKAGE_BEEEON="com.rehivetech.beeeon.debug"

# force stop
adb shell am force-stop $PACKAGE_BEEEON
adb shell ps | awk '/com\.android\.commands\.monkey/ { system("adb shell kill " $2) }'





