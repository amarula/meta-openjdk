require openjdk-common.inc

SRC_URI:append = " \
    file://0001-awt-fix-HEADLESS-compilation-without-X11.patch \
    file://0002-autoconf-libraries-drop-the-need-for-X11-in-headless.patch \
"
SRCREV = "ba94eace682733f66e623c089bcafcf4a1b8e39c"
