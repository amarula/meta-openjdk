SUMMARY = "SQLite JDBC Driver"
DESCRIPTION = "\
    SQLite JDBC is a library for accessing and creating SQLite database \
    files in Java."
HOMEPAGE = "https://github.com/xerial/sqlite-jdbc"
BUGTRACKER = "https://github.com/xerial/sqlite-jdbc/issues"
CVE_PRODUCT = "sqlite-jdbc"

SECTION = "support"

LICENSE = "Apache-2.0 & BSD-2-Clause"
LIC_FILES_CHKSUM = "\
    file://LICENSE;md5=d273d63619c9aeaf15cdaf76422c4f87 \
    file://LICENSE.zentus;md5=d393760bd738a58c439f0a8eb5305d1b \
"

S = "${WORKDIR}/git"

inherit jar maven maven_update_deps maven_ptest

SRC_URI = "git://github.com/xerial/sqlite-jdbc;protocol=https;branch=master"
SRCREV = "2b39e1f8b315fbe11cdd7271ee929ae8f9638e07"
require ${BPN}-deps.inc

# Tests that only work inside the Maven build tree:
#  - ArchUnit checks coding rules on the sources and relies on the
#    target/test-classes path to tell test code from library code.
#  - MultipleClassLoaderTest looks for target/classes and a Maven repository.
# Tests that expect writing to a read-only file or directory to fail, which
# never happens as root, the user ptest-runner runs as.
JUNIT_CONSOLE_ARGS = "\
    --exclude-engine=archunit \
    --exclude-classname=org.sqlite.MultipleClassLoaderTest \
    --exclude-methodname=org.sqlite.ConnectionTest.openNonExistingFileInReadOnlyDirectory.* \
    --exclude-methodname=org.sqlite.ErrorMessageTest.writeProtected.* \
"

do_install() {
    oe_jarinstall ${S}/target/${BPN}-${PV}.jar ${BPN}.jar
}

FILES:${PN} += "${datadir}/java/${BPN}*.jar"

RDEPENDS:${PN} = "openjdk sqlite3"

BBCLASSEXTEND = "native nativesdk"
