#!/bin/bash

PACKAGE_NAME="com.rehivetech.beeeon.debug"
EVENT=100

./startEmulator.sh --gui

#parse device name
export devices=$(adb devices | grep "device$" | head -1 |sed -e 's/\(.*\)\t.*/\1/g')
if [[ -z ${devices} ]]; then
	echo "Errro: No running devices found" >&2
	exit 1
fi

for seed in $(cat err_seeds); do
	#reinstall the app
	./install_app.sh

	seed=$(tr '\r' ' ' <<< ${seed})
	adb -s ${devices} shell monkey -s ${seed} -p ${PACKAGE_NAME} ${EVENT}

	echo "Seed ${seed} was injected, check result and press enter to continue"
	read stop
done