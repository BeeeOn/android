#!/bin/bash
#Script starts android emulator, if there are multiple devices available, the last one will be chosen
#It is neccessary to execute the script in order to start testing
#Author: David Kozak xkozak15
#If you have any question or encountered any problem, feel free to contact me : xkozak15@stud.fit.vutbr.cz

SLEEP_TIME=20

echo "Emulators available:
$(emulator -list-avds)"

NAME=$(emulator -list-avds | tail -1)
echo "Chosen last : ${NAME}"

OPTS="-no-skin -no-audio -no-window"	
if [ ! -z ${1} ] ; then 
	if [ ${1} == "--gui" ] ; then
		OPTS=""
	fi
fi

echo "Starting the emulator"
emulator -avd ${NAME} ${OPTS} &

echo "## Device is starting, now the script has to wait until the device is ready"
while [ -z $(adb devices | grep "device$" | head -1 | sed -e 's/\(.*\)\t.*/\1/g') ]  
do
	echo "## The device has not started yet, sleeping for ${SLEEP_TIME} secs and try again"
	sleep ${SLEEP_TIME}
done
echo "## The device is ready"




