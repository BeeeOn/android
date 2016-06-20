#/bin/bash
#Script reinstalls the app

export PACKAGE_NAME="com.rehivetech.beeeon.debug"

#check for parameter --download-new - it is used for getting the .apk file if neccessary
if [ ! -z ${1} ] ; then
	if [ ${1} == "--download-new" ] ; then 
		#downloads the .apk file if neccessary
		if [ ! -f ${APK_LOCATION}  ] ; then
			echo "Apk was not found, started downloading of new one" >&2
			wget ${APK_NET_PATH} --no-check-certificate	
			mv app-debug.apk ${APK_LOCATION}
		else
			echo "Apk file was found, no need to download new one"
		fi
	fi
fi

#reinstalls the app
echo "Uninstalling old version of the app"
adb -s ${devices} uninstall $PACKAGE_NAME
echo "Installing the new version"
adb -s ${devices} install ${APK_LOCATION}
