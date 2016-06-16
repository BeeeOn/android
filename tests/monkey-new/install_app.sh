#/bin/bash
#Script reinstalls the app

# TODO find a better way to use these variables than copy-past from stressTest
DIR=$(pwd) 
APK="../../artifacts/app-debug.apk"
export PACKAGE_NAME="com.rehivetech.beeeon.debug"

#check for parameter --download-new - it is used for getting the .apk file if neccessary
if [ ! -z ${1} ] ; then
	if [ ${1} == "--download-new" ] ; then 
		#downloads the .apk file if neccessary
		if [ ! -f ${DIR}/${APK}  ] ; then
			echo "Apk was not found, started downloading of new one" >&2
			wget ${APK_NET_PATH} --no-check-certificate	
		else
			echo "Apk file was found, no need to download new one"
		fi
	fi
fi

#reinstalls the app
echo "Uninstalling old version of the app"
adb -s ${devices} uninstall $PACKAGE_NAME
echo "Installing the new version"
adb -s ${devices} install ${DIR}/${APK}
