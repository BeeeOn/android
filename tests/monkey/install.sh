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

# uninstall and install Screen Recorder
echo "First uninstalling ScreenRecorder application from the device."
adb uninstall $PACKAGE_SR
echo "Installing ScreenRecorder application from the device."
adb install apk/$APK_SR

# uninstell and install BeeeOn
echo "First unistalling BeeeOn application from the device."
adb uninstall $PACKAGE_BEEEON
echo "Installing BeeeOn application from the device."
adb install apk/$APK_BEEEON

# push jar with record options
echo "Push jar archive for start and stop recording."
adb push record/bin/record.jar /data/local/tmp/







