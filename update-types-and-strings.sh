#! /bin/sh

MAIN_DIR=BeeeOn/app/src/main
PKG_ROOT_DIR=$MAIN_DIR/java/com/rehivetech/beeeon
DEVICE_TYPE_JAVA=$PKG_ROOT_DIR/household/device/DeviceType.java

cp -v generated_strings_devices.xml $MAIN_DIR/res/values/
cp -v generated_strings_devices.xml $MAIN_DIR/res/values-cs/
cp -v generated_strings_devices.xml $MAIN_DIR/res/values-sk/

awk '/\*\* BEGIN OF GENERATED CONTENT \*\*/ {exit} {print}' \
    < $DEVICE_TYPE_JAVA > DeviceType.java.tmp

cat DeviceType.java.part >> DeviceType.java.tmp
# chomp last empty line
sed -i '$d' DeviceType.java.tmp

awk 'enable == 1 {print} /\*\* END OF GENERATED CONTENT \*\*/ {enable = 1}' \
    < $DEVICE_TYPE_JAVA >> DeviceType.java.tmp

mv -v DeviceType.java.tmp $DEVICE_TYPE_JAVA
