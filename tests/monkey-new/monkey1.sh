#!/bin/bash

device=$1

echo "Executing basic monkey test:"


echo $device
echo $PACKAGE_NAME

adb -s ${device} shell monkey -p ${PACKAGE_NAME} -c android.intent.category.LAUNCHER 1;
sleep 3


for i in $(seq 1 ${ITER}) ; do
	adb shell monkey -p ${PACKAGE_NAME} ${EVENT}
	sleep 1
done 


#PID=$(adb shell ps | grep ${APP_NAME} | tr -s ' ' | cut -d ' ' -f 2)
#echo "PID of the beeon app process is : ${PID}, killing..."
#adb -s ${device} shell kill ${PID}


adb shell am force-stop ${PACKAGE_NAME}

echo "Test finished"
