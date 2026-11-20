require openjdk-common.inc

SRC_URI:append = " \
    file://0001-awt-fix-HEADLESS-compilation-without-X11.patch \
    file://0002-autoconf-libraries-drop-the-need-for-X11-in-headless.patch \
"
SRC_BRANCH = "jdk21.0.13.1"
SRCREV = "7a7090bd432d6e8b4e6106593957513a84e7add4"
