#!/bin/bash

ant build
adb push bin/test.jar /data/local/tmp/
adb shell uiautomator runtest test.jar -c com.rehivetech.beeeon.TestDemo

# adb push  /sdcard/
# adb shell uiautomator runtest /sdcard/ -c
# adb shell uiautomator runtest /sdcard/MyUIAutomatorTest.jar -c com.looksok.uiautomator.TestSampleBackButton
# -c com.looksok.uiautomator.TestSampleBackButton#testMethod
