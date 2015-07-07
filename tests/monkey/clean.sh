#!/bin/bash
#------------------------------------------------------------------------------
#
#
#
#
# 
#------------------------------------------------------------------------------

APK_BEEEON="app-debug.apk"
PACKAGE_BEEEON="com.rehivetech.beeeon.debug"

APK_SR="base.apk"
PACKAGE_SR="com.nll.screenrecorder"

# uninstall
echo "Uninstalling Screen Recorder application from the device."
adb uninstall $PACKAGE_SR 
echo "Uninstalling BeeeOn application from the device."
adb uninstall $PACKAGE_BEEEON

# cleaning from device
echo "Cleaning jar archives from the device."
adb shell rm /data/local/tmp/record.jar
adb shell rm /data/local/tmp/test.jar




