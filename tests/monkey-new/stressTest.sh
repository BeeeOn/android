#!/bin/bash
#Main script for stress testing of BeeeOn, reinstalls the app on the phone and execute the tests $ITER times with $EVENT strength
#The device for test execution can be either passed into the script as the first param or it will be chosen automatically
#Author: David Kozak xkozak15
#If you have any question or encountered any problem, feel free to contact me : xkozak15@stud.fit.vutbr.cz

export APP_NAME="beeeon"
export LOG_FILE="log.out"
export ERR_FILE="log.err"
export PACKAGE_NAME="com.rehivetech.beeeon.debug"
export DIR=$(pwd)   #"/home/xkozak15/"
export EVENT=100
export ITER=5
export APK="app-debug.apk"
export APK_NET_PATH="https://ant-2.fit.vutbr.cz:8443/jenkins/job/android-app/lastSuccessfulBuild/artifact/BeeeOn/app/build/outputs/apk/app-debug.apk"

function start_emulator(){
	#NAME=$(emulator -list-avds | head -1)
	echo "Please start at least one emulator in different process" >&2
	exit;
}

#prepare output files
if [ ! -f ${DIR}/${LOG_FILE} ] ; then
	touch ${LOG_FILE}
fi

if [ ! -f ${DIR}/${ERR_FILE} ] ; then
	touch ${ERR_FILE}
fi


#downloads the .apk file if neccessary
if [ ! -f ${DIR}/${APK}  ] ; then
	echo "Apk was not found, started downloading of new one" >&2
	wget ${APK_NET_PATH} --no-check-certificate	
else
	echo "Apk file was found, no need to download new one"
fi

#checks if the device was specified as argument
if [ ! -z "$1" ] ; then
	if [ -z "$(adb devices | grep "${1}")" ] ; then
		echo "Device ${1} was not found, exiting..."
		exit 1
	fi
	devices=$1 
else 
	devices=$(adb devices | grep "device$" |sed -e 's/\(.*\)\t.*/\1/g')
fi

#checks if the device is set correctly
if [ -z "$devices" ] ; then 
	echo "No running devices found, starting a new emulator" >&2
	./start_emulator.sh
	sleep 60
	devices=$(adb devices | grep "device$" |sed -e 's/\(.*\)\t.*/\1/g')

elif (( $(grep -c . <<<"$devices") > 1 )) ; then
	echo "Two or more devices are running, the first one will be chosen for testing" >&2
	devices=$(adb devices | grep "device$" | head -1 |sed -e 's/\(.*\)\t.*/\1/g')
fi

echo "Device chosen for testing is $devices"

#reinstalls the app
echo "Uninstalling old version of the app"
adb -s ${devices} uninstall $PACKAGE_NAME
echo "Installing the new version"
adb -s ${devices} install ${DIR}/${APK}

#executes the tests
for i in $(seq 1 ${ITER}) ; do
	echo "									Iteration no ${i}"
	./monkey1.sh ${devices} 2>>${ERR_FILE} >> ${LOG_FILE}
	
done


#kill all emulators to clean the server
devices=$(adb devices | tail -n+2 | sed -e 's/\(.*\)\t.*/\1/g')
echo "Testing was finished devices ${devices} will be killed now"
for device in ${devices} ; do
	adb -s ${device} emu kill
done

