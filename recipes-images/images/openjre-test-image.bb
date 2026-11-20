SUMMARY = "Image for testing an OpenJDK JRE (BUILD_FULL_JDK = \"false\")"
DESCRIPTION = "\
    Boots with an OpenJDK JRE and runs the OpenJDK runtime tests, \
    using a test program compiled on the build host."

inherit core-image

require openjdk-test-image.inc

# A JRE has no javac, so the test program is compiled on the build host.
IMAGE_INSTALL:append = " openjdk-test-jar"
