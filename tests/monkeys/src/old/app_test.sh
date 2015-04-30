#!/bin/bash
#-------------------------------------------------------------------------------
# This script:
# 1. Install the application
# 2. Launch the application
# 3. Test the application using monkey
# 4. Uninstall the applications
#
# Script collects logs on each step.
#-------------------------------------------------------------------------------

APK="example.apk"
PACKAGE="com.example.package"
ACTIVITY="com.example.package.activity"

rm -rf log
mkdir log

# 1. Install the application
adb uninstall $PACKAGE # Uninstall application if already installed
adb logcat -c # Clear log buffer
adb install $APK # Install the application
adb logcat -d > log/install.log # Write application install logs

# 2. Launch the application
adb logcat -c
adb shell am start -n $PACKAGE/$ACTIVITY # Launch the application
sleep 10 # wait for 10 sec
adb logcat -d > log/start.log

# 3. Test the application
adb logcat -c
# Test the application using Monkey
adb shell monkey --pct-touch 70 -p $PACKAGE -v 1000 --throttle 500
adb logcat -d > log/test.log

# 4. Uninstall the application
adb logcat -c
adb uninstall $PACKAGE
adb logcat -d > log/uninstall.log
