from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
from com.android.monkeyrunner.easy import EasyMonkeyDevice, By 
import time

def main():
	device = initDevice()
	easyDevice = EasyMonkeyDevice(device)
	easyDevice.touch(By.id('id/login_demo_button'),MonkeyDevice.DOWN_AND_UP)
	time.sleep(1)
	easyDevice.touch(By.id('id/devices_list_fab'),MonkeyDevice.DOWN_AND_UP)

	print("Test finished")
	time.sleep(1)

	# Takes a screenshot
	result = device.takeSnapshot()
	# Writes the screenshot to a filea
	result.writeToFile('shot1.png','png')

def initDevice():
	print("Waiting for device")	
	device = MonkeyRunner.waitForConnection()

	device.press('KEYCODE_HOME',MonkeyDevice.DOWN_AND_UP)
	kill_command = 'am force-stop %s' % 'com.rehivetech.beeeon.debug'
	device.shell(kill_command)
	time.sleep(1)

	apk_path = device.shell('pm path com.rehivetech.beeeon.debug')
	if apk_path.startswith('package:'):
	    print "BeeeOn already installed."
	else:
	    print "BeeeOn not installed, installing APKs..."
	    device.installPackage('/home/david/Documents/BeeeOn/tests/monkey-new/app-debug.apk')


	# sets a variable with the package's internal name
	package = 'com.rehivetech.beeeon.debug'

	# sets a variable with the name of an Activity in the package
	activity = 'com.rehivetech.beeeon.gui.activity.LoginActivity'

	# sets the name of the component to start
	runComponent = package + '/' + activity

	print("Starting activity " + runComponent)

	# Runs the component
	device.startActivity(component=runComponent)

	time.sleep(1)
	return device

if __name__ == '__main__':
	main()
