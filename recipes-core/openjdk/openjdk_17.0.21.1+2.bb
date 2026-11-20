require openjdk-common.inc

SRC_URI:append = " \
    file://0001-awt-fix-HEADLESS-compilation-without-X11.patch \
"
SRC_BRANCH = "jdk17.0.21.1"
SRCREV = "10ebaa9a96d18973cfd08ec6828965886945f14f"
