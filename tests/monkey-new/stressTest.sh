#!/bin/bash

export APP_NAME="beeeon"
export PACKAGE_NAME="com.rehivetech.beeeon.debug"
export DIR=$(pwd)   #"/home/xkozak15/"
export EVENT=100
export ITER=5
export APK="app-debug.apk"
export APK_NET_PATH="https://ant-2.fit.vutbr.cz:8443/jenkins/job/android-app/lastSuccessfulBuild/artifact/BeeeOn/app/build/outputs/apk/app-debug.apk"

function start_emulator(){
	echo "HERE"
}


if [ ! -f ${DIR}/${APK}  ] ; then
	echo "Apk was not found, started downloading of new one" >&2
	wget ${APK_NET_PATH} --no-check-certificate	
else
	echo "Apk file was found, no need to download new one"
fi

devices=$(adb devices | grep "device$" |sed -e 's/\(.*\)\t.*/\1/g')

if [ -z "$devices"   ] ; then 
	echo "No running devices found, starting a new emulator" >&2
	start_emulator
elif (( $(grep -c . <<<"$devices") > 1 )) ; then
	echo "Two or more devices are running, the first one will be chosen for testing" >&2
	devices=$(adb devices | grep "device$" | head -1 |sed -e 's/\(.*\)\t.*/\1/g')
fi

echo "Device chosen for testing is $devices"

adb -s ${devices} uninstall $PACKAGE_NAME
adb -s ${devices} install ${DIR}/${APK}

for i in $(seq 1 ${ITER}) ; do
	echo "Iteration no ${i}"
	./monkey1.sh ${devices}
	
done
