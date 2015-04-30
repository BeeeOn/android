#!/bin/bash
#-------------------------------------------------------------------------------
# This script:
# 1. Install the application
# 2. Launch the application
# 3. Test the application using monkey
#
# Script collects logs on each step.
#-------------------------------------------------------------------------------

APK_BEEEON="app-debug.apk"
PACKAGE_BEEEON="com.rehivetech.beeeon.debug"
ACTIVITY_BEEEON="com.rehivetech.beeeon.activity.LoginActivity"

APK_SR="Screen_Recorder.apk"
PACKAGE_SR="com.nll.screenrecorder"
ACTIVITY_SR="com.nll.screenrecorder.activity.RouterActivity"

LOG_DIR="logs/log-d-small"

mkdir $LOG_DIR

#touch $DIR/install.log
#touch $DIR/start.log
#touch $DIR/test.log
#rm -f xx
#mkdir xx

# 1. run screen recorder video
#/home/martina/Android/Sdk/platform-tools/adb logcat -c
#adb shell am start -n $PACKAGE_SR/$ACTIVITY_SR
#adb shell am start -n com.nll.screenrecorder/com.nll.screenrecorder.activity.RouterActivity
#/home/martina/Android/Sdk/platform-tools/adb shell "am start -n com.nll.screenrecorder/com.nll.screenrecorder.activity.RouterActivity"
#sleep 10 # wait for 5 sec
#/home/martina/Android/Sdk/platform-tools/adb logcat -d > logs/monkey-start/start_sr.log
# start recording
#adb shell am start -n com.nll.screenrecorder
adb shell uiautomator runtest record.jar -c Record#testStart
# 2. Launch the application
adb logcat -c
#adb shell am start -n $PACKAGE_BEEEON/$ACTIVITY_BEEEON # Launch the application
adb shell am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.activity.LoginActivity 
# Launch the application
sleep 5 # wait for 5 sec
adb logcat -d > $LOG_DIR/start.log
# 3. Test the application
adb logcat -c
# Test the application using Monkey
#adb shell monkey -p $PACKAGE_BEEEON -vvv --pct-majornav 0 --pct-syskeys 0 1000 #--throttle 500 
adb shell monkey -p com.rehivetech.beeeon.debug -vvv --pct-majornav 0 --pct-syskeys 0 1000 #--throttle 500 
adb logcat -d > $LOG_DIR/test.log
# stop recording 
adb shell uiautomator runtest record.jar -c Record#testStop

# pull video to folder, where is logcat