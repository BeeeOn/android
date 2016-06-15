#!/bin/bash

./start_emulator.sh

./stress_test.sh

echo "Testing was finished, now I kill all emulators left(if there are any)"

for emu in $(pgrep emulator64-x86)
do
        echo "Killing proc ${emu}"
        kill -9 ${emu}
done

echo "All emulators are dead, exiting..."