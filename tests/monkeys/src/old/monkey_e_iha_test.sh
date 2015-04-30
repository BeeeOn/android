#!/bin/bash
#-------------------------------------------------------------------------------
# This script:
# 1. Launch the application
# 2. Test the application using monkey
#
# Script collects logs on each step.
#-------------------------------------------------------------------------------

APK="IHA.apk"
PACKAGE="cz.vutbr.fit.iha"
ACTIVITY="cz.vutbr.fit.iha.LoginActivity"

rm -rf log
mkdir log

# 1. Launch the application
adb logcat -c
adb -e shell am start -n $PACKAGE #/$ACTIVITY # Launch the application
sleep 10 # wait for 10 sec
adb logcat -e > log/start.log

# 2. Test the application
adb logcat -c
# Test the application using Monkey
adb -e shell monkey -p $PACKAGE -vvv --pct-majornav 0 --pct-syskeys 0 50000 #--throttle 500 
adb logcat -e > log/test.log