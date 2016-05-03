from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
from com.android.monkeyrunner.easy import EasyMonkeyDevice, By 

class DeviceHandler():
	package = 'com.rehivetech.beeeon.debug'
	main_activity = 'com.rehivetech.beeeon.gui.activity.LoginActivity'
	apk_path = '../app-debug.apk'

	def __init__(self,insert_sleeps = True):
		print("Waiting for device")	
		self.device = MonkeyRunner.waitForConnection()
		self.easyDevice = EasyMonkeyDevice(self.device)
		self.insert_sleeps = insert_sleeps
		self.device.press('KEYCODE_HOME',MonkeyDevice.DOWN_AND_UP)
		kill_command = 'am force-stop %s' % DeviceHandler.package
		self.device.shell(kill_command)
		self.insert_sleep(time=1)

		apk_path = self.device.shell('pm path ' + DeviceHandler.package)
		if apk_path.startswith('package:'):
		    print "BeeeOn already installed."
		else:
		    print "BeeeOn not installed, installing APKs..."
		    device.installPackage(DeviceHandler.apk_path)

		# sets the name of the component to start
		runComponent = DeviceHandler.package + '/' + DeviceHandler.main_activity

		print("Starting activity " + runComponent)

		# Runs the component
		self.device.startActivity(component=runComponent)
		self.insert_sleep()

	def insert_sleep(self,time=3):
		if self.insert_sleep:
			MonkeyRunner.sleep(time)

	def press_button(self,id):
		self.easyDevice.touch(By.id('id/' + id),MonkeyDevice.DOWN_AND_UP)
		self.insert_sleep()

	def touch_the_screen(self,x,y):
		self.device.touch(x,y,MonkeyDevice.DOWN_AND_UP)
		self.insert_sleep()

	def type_text(self,text):
		self.device.type(text)
		self.insert_sleep(time=3)

	def take_snapshot(self):
		return self.device.takeSnapshot()

	def save_snapshot(self,filename,extension):
		snapshot = self.take_snapshot()
		snapshot.writeToFile(filename + '.' + extension,extension)

	def compare_snapshots(self,file_name):
		snapshot = self.take_snapshot()
		other = MonkeyRunner.loadImageFromFile(file_name)
		if not snapshot.sameAs(other,0.9):
			self.error("Snaphots are not equal")
		else:
			print("OK")

	def error(self,msg):
		print(msg)
