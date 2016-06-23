from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
from com.android.monkeyrunner.easy import EasyMonkeyDevice, By 

import sys
from sys import stdin
import os
import subprocess

class DeviceHandler():
	package = 'com.rehivetech.beeeon.debug'
	main_activity = 'com.rehivetech.beeeon.gui.activity.LoginActivity'
	apk_path = '../../../artifacts/app-debug.apk'

	def __init__(self,insert_sleeps = True):
		print("Waiting for device")	
		self.device = MonkeyRunner.waitForConnection()
		self.easyDevice = EasyMonkeyDevice(self.device)
		self.insert_sleeps = insert_sleeps
		self.device.press('KEYCODE_HOME',MonkeyDevice.DOWN_AND_UP)
		kill_command = 'am force-stop %s' % DeviceHandler.package
		self.device.shell(kill_command)
		self.insert_sleep(time=3)
		self.err_snapshot_count = 0

		apk_path = self.device.shell('pm path ' + DeviceHandler.package)
		if apk_path.startswith('package:'):
		    print "BeeeOn already installed,but I am gonna reinstall it to test the newest version"
		    self.device.installPackage(DeviceHandler.apk_path)
		else:
		    print "BeeeOn not installed, installing APKs..."
		    self.device.installPackage(DeviceHandler.apk_path)

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
		try:
			self.easyDevice.touch(By.id('id/' + id),MonkeyDevice.DOWN_AND_UP)
			self.insert_sleep()
		except Exception, e:
			print('Button was not found,saving snapshot of the screen')
			self.save_snapshot('button_not_found' + str(self.err_snapshot_count),'png')
			self.err_snapshot_count += 1
			raise e

	def touch_the_screen(self,x,y):
		self.device.touch(x,y,MonkeyDevice.DOWN_AND_UP)
		#subprocess.Popen(['adb','shell' ,'input' ,'tap' ,str(x), str(y)])
		#self.device.shell('input tap ' + str(x) + ' ' + str(y))
		self.insert_sleep()

	def touch_the_screen_on_given_coordinates(self):
		while True:
			try:
				print("Give me coordinates x y : ")
				a = stdin.readline()
				a = a.split(' ')
				x = int(a[0])
				y = int(a[1])
				print("Coordinates: " + a[0] + " " + a[1])
				self.touch_the_screen(x,y)
				break
			except:
				print("Input was not valid, try again...")


	def type_text(self,text):
		self.device.type(text)
		self.insert_sleep()

	def take_snapshot(self):
		return self.device.takeSnapshot()

	def save_snapshot(self,filename,extension):
		snapshot = self.take_snapshot()
		snapshot.writeToFile(filename + '.' + extension,extension)

	def compare_snapshots(self,file_name):
		snapshot = self.take_snapshot()
		other = MonkeyRunner.loadImageFromFile(file_name)
		if not snapshot.sameAs(other,0.9):
			snapshot.writeToFile('add_gateway_bad_result.png','png')
			self.error("Snaphots are not equal") 
			sys.exit(1)
		else:
			print("Snapshots are equal")

	def error(self,msg):
		print(msg)
		sys.exit(1)
