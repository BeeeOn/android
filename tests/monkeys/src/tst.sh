#!/bin/bash
#------------------------------------------------------------------------------
#
#
#
#
# 
#------------------------------------------------------------------------------

PACKAGE_BEEEON="com.rehivetech.beeeon.debug"
ACTIVITY_BEEEON="com.rehivetech.beeeon.activity.LoginActivity"

PACKAGE_SR="com.nll.screenrecorder"
ACTIVITY_SR="com.nll.screenrecorder.activity.RouterActivity"

LOG_DIR="logs/tst"
mkdir logs
mkdir $LOG_DIR

if [ $# -ge 1 ]
then
	SEED="-s $1"
fi

# save timestamp
DATE=`date +%Y%m%d%H%M%S`
#DATE=$(date +%Y%m%d%H%M%S)

# start recording
adb shell uiautomator runtest record.jar -c Record#testStart
adb logcat -c

# start BeeeOn application
adb logcat -c
adb shell am start -n $PACKAGE_BEEEON/$ACTIVITY_BEEEON
sleep 2

# start monkey test
if [ $# -ge 1 ]
then 
	adb -d shell monkey -p $PACKAGE_BEEEON -vvv --pct-majornav 0 --pct-syskeys 0 $SEED 1000
else
	adb -d shell monkey -p $PACKAGE_BEEEON -vvv --pct-majornav 0 --pct-syskeys 0 1000
fi
adb logcat -d > $LOG_DIR/logcat.$((DATE+3)).log

# stop recording
adb shell uiautomator runtest record.jar -c Record#testStop
adb logcat -c

# pull video to foldet where the logcat ouptut is located
adb pull /mnt/shell/emulated/0/ScreenRecorder/ $LOG_DIR/
adb shell rm -r /mnt/shell/emulated/0/ScreenRecorder


