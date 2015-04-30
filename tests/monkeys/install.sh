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

LOG_DIR="logs/monkey-install"

mkdir logs
mkdir $LOG_DIR

# uninstall
adb uninstall $PACKAGE_BEEEON
adb uninstall $PACKAGE_SR

adb logcat -c
adb install apk/$APK_SR
adb logcat -d > $LOG_DIR/install_sr.log

adb uninstall $PACKAGE_BEEEON
adb logcat -c
adb install apk/$APK_BEEEON
adb logcat -d > $LOG_DIR/install_beeeon.log

adb push record/bin/record.jar /data/local/tmp/

# root for screen recorder, android < 5.0





