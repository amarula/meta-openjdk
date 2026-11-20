SUMMARY = "A tool that for building and managing Java-based projects"
DESCRIPTION = "\
    Maven provides a standard way to build the projects, a clear definition \
    of what the project consisted of, an easy way to publish project \
    information, and a way to share JARs across several projects."

HOMEPAGE = "https://maven.apache.org"
BUGTRACKER = "https://github.com/apache/maven/issues"
CVE_PRODUCT = "maven"

SECTION = "devtools"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=dfc679e42f6af0f38970f0a2bd9122d9"

DEPENDS += "\
    openjdk-bin-native \
    rsync-native \
"
SRC_URI = "https://dlcdn.apache.org/maven/maven-3/${PV}/binaries/apache-maven-${PV}-bin.tar.gz"
SRC_URI[sha256sum] = "810b8348f0a40f9116c202093fae007ebeabf67699bf34d81ee7e272289e085a"

S = "${UNPACKDIR}/apache-maven-${PV}"

do_install() {
    install -d ${D}${libdir}/maven
    rsync -acIr --no-perms --no-owner --no-group ${S}/ ${D}${libdir}/maven/
    rm -rf ${D}${libdir}/maven/lib/jline-native/FreeBSD
    rm -rf ${D}${libdir}/maven/lib/jline-native/Windows
    rm -rf ${D}${libdir}/maven/lib/jline-native/Linux/arm*
    rm -rf ${D}${libdir}/maven/lib/jline-native/Linux/ppc*
    rm -rf ${D}${libdir}/maven/lib/jline-native/Linux/x86
}

FILES:${PN} += "${libdir}/maven"

BBCLASSEXTEND = "native nativesdk"
