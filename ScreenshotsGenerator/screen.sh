# Script for automate capturing screenshots
# Params:
#		$1 ... device 
#		$2 ... language (en, cs, sk)
#		$3 ... target dir

# check number of parametres
if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters."
fi

# emulator name = Nexus_2_API_21_x86
# TODO in main script

# set required language
if [ "$2" = "en" ]; then
	adb shell "setprop persist.sys.language $2;
			   setprop persist.sys.country GB;
			   stop;
			   sleep 2;
			   start"
fi

if [ "$2" = "cs" ]; then
	adb shell "setprop persist.sys.language $2;
			   setprop persist.sys.country CZ;
			   stop;
			   sleep 2;
			   start"
fi

if [ "$2" = "sk" ]; then
	adb shell "setprop persist.sys.language $2;
			   setprop persist.sys.country SK;
			   stop;
			   sleep 2;
			   start"
fi

# need for reboot
sleep 20

# create dir for screenshots in device
adb shell "mkdir /data/local/tmp/gpss"

# unlock phone
adb shell input keyevent 82

# 1 Intro screen
adb shell "am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.gui.activity.IntroActivity"
sleep 2
adb shell "screencap -p /data/local/tmp/gpss/screenshot1.png"

# 2 Login screen 
adb shell "am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.gui.activity.LoginActivity"
sleep 5
adb shell "screencap -p /data/local/tmp/gpss/screenshot2.png"

# Overview screen
adb shell "am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.gui.activity.MainActivity"
sleep 7
adb shell "screencap -p /data/local/tmp/gpss/screenshot3.png"

# navDrawer
adb shell input tap 50 90
sleep 5
adb shell "screencap -p /data/local/tmp/gpss/screenshot5.png"

# Overview screen
adb shell "am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.gui.activity.ModuleDetailActivity -e gate_id 65260 -e module_id 101:00:FF:000:FF0---10"
sleep 20
adb shell "screencap -p /data/local/tmp/gpss/screenshot4.png"

# Guige screen
adb shell "am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.gui.activity.AddGateActivity"
sleep 5
adb shell "screencap -p /data/local/tmp/gpss/screenshot6.png"

# Settings screen
adb shell "am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.gui.activity.SettingsMainActivity"
sleep 5
adb shell "screencap -p /data/local/tmp/gpss/screenshot7.png"

# pull screenshots to PC
adb pull /data/local/tmp/gpss $3

# cleaning in device
adb shell "rm -r /data/local/tmp/gpss"
