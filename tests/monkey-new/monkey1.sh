#!/bin/bash
#Script Starts simple monkey stress test, 
#Author: David Kozak xkozak15
#If you have any question or encountered any problem, feel free to contact me : xkozak15@stud.fit.vutbr.cz

device=$1
iter=$2

if [ -z "${device}" ] ; then
	echo "No device was specified, exiting..." >&2
	exit 1
elif [ -z "$(adb devices | grep "${device}")" ] ; then
	echo "Device ${device} was not found, exiting..."
	exit 1
fi

echo "
---------------------------------------------------------------------------------------------------
"
echo "Executing basic monkey test:"

#launch main activity
adb -s ${device} shell monkey -p ${PACKAGE_NAME} -c android.intent.category.LAUNCHER 1;
sleep 3

if [[ "$iter" -eq "1" ]]; then
	echo "first time"
	for i in $(seq 1 5) 
	do
		adb -s ${device} shell input tap 450 750
	done
fi

adb -s ${device} shell input tap 200 750

adb -s emulator-5554 shell input tap 350 500

# now put random monkey taps
adb -s ${device} shell monkey -p ${PACKAGE_NAME} ${EVENT}  2>>${ERR_FILE}
sleep 1



#PID=$(adb shell ps | grep ${APP_NAME} | tr -s ' ' | cut -d ' ' -f 2)
#echo "PID of the beeon app process is : ${PID}, killing..."
#adb -s ${device} shell kill ${PID}

#shut down the application
adb -s ${device} shell am force-stop ${PACKAGE_NAME}

echo "Test finished"
echo "
---------------------------------------------------------------------------------------------------
"
