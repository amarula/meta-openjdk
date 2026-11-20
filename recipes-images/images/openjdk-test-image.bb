SUMMARY = "Image for testing a full OpenJDK (BUILD_FULL_JDK = \"true\")"
DESCRIPTION = "\
    Boots with a full OpenJDK and runs the OpenJDK runtime tests, \
    compiling the test program with javac on the target."

inherit core-image

require openjdk-test-image.inc

# The tests compile their test program with javac on the target.
OPENJDK_TEST_JDK = "1"
