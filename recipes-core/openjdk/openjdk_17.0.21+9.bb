require openjdk-common.inc

SRC_URI:append = " \
    file://0001-awt-fix-HEADLESS-compilation-without-X11.patch \
"
SRCREV = "e8adc958475b92b9f251145c6ce20532be16d806"
