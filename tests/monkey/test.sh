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

COUNT=10000

LOG_DIR="logs/test"
mkdir logs > /dev/null
mkdir $LOG_DIR > /dev/null

if [ $# -ge 1 ]
then
	SEED="-s $1"
fi

# save timestamp
DATE=`date +%Y%m%d%H%M%S`

# start recording
echo "record: "
echo "	Start recording the test."
adb shell uiautomator runtest record.jar -c Record#testStart > /dev/null
sleep 2

# start BeeeOn application
adb shell am start -n $PACKAGE_BEEEON/$ACTIVITY_BEEEON > /dev/null

echo "pin: "
echo "	Pin screen of testing application. Press MENU button and click on the 'pin' icon."
echo "	Waiting 10s ..."
sleep 15

echo "test: "
echo "	Performing monkey test. Now the device will receive $COUNT user events and gestures."
echo "	The test will take 2 minutes approximately."
# start monkey test

adb logcat -c
if [ $# -ge 1 ]
then 
	adb shell monkey -p $PACKAGE_BEEEON -vvv $SEED $COUNT > $LOG_DIR/logmonkey.$((DATE+3)).log
else
	adb shell monkey -p $PACKAGE_BEEEON -vvv $COUNT > $LOG_DIR/logmonkey.$((DATE+3)).log
fi
echo "	Monkey test finished."

adb logcat > $LOG_DIR/logcat.$((DATE+3)).log
echo "logs: "
echo "	Saving the output of the testing to $LOG_DIR/logmonkey.$((DATE+3)).log."
echo "	Saving the logcat from device to $LOG_DIR/logcat.$((DATE+3)).log."

echo "unpin: "
echo "	Unpin your app with pressing of MENU and BACK button at the same time."
echo "	Waiting 10s ..."
sleep 15

# stop recording
echo "record: "
echo "	Stop recording the test."
adb shell uiautomator runtest record.jar -c Record#testStop > /dev/null

# pull video to foldet where the logcat ouptut is located
echo "pull: "
echo "	Pull the video from the device."
adb pull /mnt/shell/emulated/0/ScreenRecorder/ $LOG_DIR/ > /dev/null
adb shell "rm -r /mnt/shell/emulated/0/ScreenRecorder" > /dev/null

echo "results: "
echo "	See results of the monkey test in logs/test."
echo "	If you want to start the same test again. Search for 'SEED' value in log file"
echo "	and start the test with that value as a parameter (sh test.sh [SEED])."


