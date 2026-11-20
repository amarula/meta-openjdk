SUMMARY = "Jar used by the OpenJDK runtime tests"
DESCRIPTION = "\
    Runnable jar for the OpenJDK runtime tests on images without javac. \
    The tests compile the same source on images with a full JDK."
HOMEPAGE = "https://github.com/amarula/meta-openjdk"
BUGTRACKER = "https://github.com/amarula/meta-openjdk/issues"
SECTION = "devel"
# A test program, not a product tracked for CVEs.
CVE_PRODUCT = ""

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://TheAnswer.java;beginline=1;endline=1;md5=6ec41034e04432ee375d0e14fba596f4"

inherit allarch jar

DEPENDS += "openjdk-bin-native"

SRC_URI = "file://TheAnswer.java"

S = "${WORKDIR}"

OPENJDK_NATIVE_BIN = "${STAGING_DIR_NATIVE}${libdir_native}/jvm/bin"

do_compile() {
    ${OPENJDK_NATIVE_BIN}/javac \
        --release ${@get_openjdk_version(d)} \
        -d ${B}/classes \
        ${S}/TheAnswer.java
    ${OPENJDK_NATIVE_BIN}/jar \
        --create \
        --file ${B}/${BPN}.jar \
        --main-class TheAnswer \
        -C ${B}/classes .
}
do_compile[cleandirs] = "${B}/classes"

do_install() {
    oe_jarinstall ${B}/${BPN}.jar
}

FILES:${PN} += "${datadir_java}"

RDEPENDS:${PN} = "openjdk"

BBCLASSEXTEND = "native"
