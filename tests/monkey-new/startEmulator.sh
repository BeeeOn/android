#!/bin/bash
#Script starts android emulator, if there are multiple devices available, the last one will be chosen
#It is neccessary to execute the script in order to start testing
#Author: David Kozak xkozak15
#If you have any question or encountered any problem, feel free to contact me : xkozak15@stud.fit.vutbr.cz

echo "Emulators available:
$(emulator -list-avds)"

NAME=$(emulator -list-avds | tail -1)
echo "Chosen last : ${NAME}"

echo "Starting the emulator"
emulator -avd ${NAME} -no-skin -no-audio -no-window &
