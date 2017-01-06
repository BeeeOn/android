#!/bin/bash

export API_KEYS_LOCATION=/opt/redmine/files/2016/03/160329222230_api_keys.xml
export KEYSTORE_LOCATION=/opt/redmine/files/2015/12/151204101534_debug.keystore
export ANDROID_HOME=/opt/android-sdk-linux
export JAVA_HOME=/usr/lib/jvm/java-8-oracle

cp -v "${API_KEYS_LOCATION}" BeeeOn/app/src/main/res/values/api_keys.xml || exit -1
mkdir -p BeeeOn/keystores && cp -v "${KEYSTORE_LOCATION}" BeeeOn/keystores/debug.keystore || exit -1
cd BeeeOn && ./gradlew assembleDebug

cp app/build/outputs/apk/app-debug.apk ../.
