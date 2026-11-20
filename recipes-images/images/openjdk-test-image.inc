# Common parts of the OpenJDK test images.
#
# The OpenJDK version under test is the one selected for the build with
# PREFERRED_VERSION_openjdk. Whether that is a full JDK or a JRE depends on
# BUILD_FULL_JDK, so each image needs its own build directory.

HOMEPAGE = "https://github.com/amarula/meta-openjdk"
BUGTRACKER = "https://github.com/amarula/meta-openjdk/issues"
SECTION = "images"
LICENSE = "MIT"

IMAGE_FEATURES += "ssh-server-dropbear"

# Java libraries from this layer. jsch needs OpenJDK 25 or newer.
OPENJDK_TEST_LIBRARIES = "\
    sqlite-jdbc \
    ${@'jsch' if openjdk_version_at_least(d, '25') else ''} \
"
OPENJDK_TEST_PTESTS = "${@' '.join(f'{lib}-ptest' for lib in d.getVar('OPENJDK_TEST_LIBRARIES').split())}"

IMAGE_INSTALL = "\
    packagegroup-core-boot \
    ca-certificates-java \
    openjdk \
    ${OPENJDK_TEST_LIBRARIES} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'ptest', 'ptest-runner ${OPENJDK_TEST_PTESTS}', '', d)} \
    ${CORE_IMAGE_EXTRA_INSTALL} \
"

# The major version the tests expect java -version to report.
OPENJDK_TEST_VERSION = "${@get_openjdk_version(d)}"

TEST_SUITES = "\
    ping \
    ssh \
    openjdk \
    ${@bb.utils.contains('DISTRO_FEATURES', 'ptest', 'ptest', '', d)} \
"
