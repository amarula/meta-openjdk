SUMMARY = "Common CA certificates (JKS trustStore)"
DESCRIPTION = "\
    This package uses the hooks of the ca-certificates \
    package to update the cacerts JKS trustStore used for many java runtimes. \
"
HOMEPAGE = "https://salsa.debian.org/java-team/ca-certificates-java"
BUGTRACKER = "https://salsa.debian.org/java-team/ca-certificates-java/-/merge_requests"
CVE_PRODUCT = ""
SECTION = "base"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "\
    file://debian/copyright;md5=ab0f6b6900f6564dc3e273dfa36fcc72 \
    file://src/main/java/org/debian/security/InvalidKeystorePasswordException.java;endline=17;md5=f9150bf1ca3139a38ddb54f9e1c0eb9b \
    file://src/main/java/org/debian/security/KeyStoreHandler.java;endline=18;md5=3fd0e26abbca2ec481cf3698431574ae \
    file://src/main/java/org/debian/security/UnableToSaveKeystoreException.java;endline=17;md5=f9150bf1ca3139a38ddb54f9e1c0eb9b \
    file://src/main/java/org/debian/security/UnknownInputException.java;endline=17;md5=f9150bf1ca3139a38ddb54f9e1c0eb9b \
    file://src/main/java/org/debian/security/UpdateCertificates.java;endline=18;md5=3fd0e26abbca2ec481cf3698431574ae \
"
DEPENDS = "openjdk-bin-native"
PACKAGE_WRITE_DEPS += "openjdk-bin-native"

SRC_URI = "\
    git://salsa.debian.org/java-team/ca-certificates-java.git;protocol=https;branch=master;tag=debian/${PV};\
    file://0001-UpdateCertificates-handle-SYSROOT-environment-variab.patch \
    file://${BPN}.hook.in \
"
SRCREV = "7bb4be7f93cb20ba0d21f60effdcf6f6e106ce8d"

inherit jar

JARFILENAME = "${BPN}.jar"

do_fix_sysconfdir () {
    sed -e 's|/etc/ssl/certs/java|${sysconfdir}/ssl/certs/java|g' \
        -i ${S}/src/main/java/org/debian/security/UpdateCertificates.java
}
do_fix_sysconfdir[doc] = "Replace the hardcoded /etc/ssl/certs/java path with ${sysconfdir}/ssl/certs/java"

do_compile () {
    install -d build # simplify in-tree builds (externalsrc)
    ${STAGING_DIR_NATIVE}/${libdir}/jvm/bin/javac \
        -g \
        -source "${@get_openjdk_version(d)}" \
        -target "${@get_openjdk_version(d)}" \
        -encoding ISO8859-1 \
        -d build \
        -sourcepath ${S}/src/main/java \
        $(find ${S}/src/main/java -name '*.java' -type f)

    # needs to end with two empty lines
    printf "Manifest-Version: 1.0\n" > ${B}/manifest
    printf "Main-Class: org.debian.security.UpdateCertificates\n\n\n" >> ${B}/manifest
    ${STAGING_DIR_NATIVE}/${libdir}/jvm/bin/jar -cfm ${JARFILENAME} ${B}/manifest -C build .
}

do_install () {
    oe_jarinstall ${JARFILENAME}

    install -d ${D}${sysconfdir}/ssl/certs/java
    install -Dm0755 ${UNPACKDIR}/${BPN}.hook.in ${D}${sysconfdir}/ca-certificates/update.d/${BPN}-hook
    sed -i \
        -e 's#@@BPN@@#${BPN}#g' \
        -e 's#@@datadir_java@@#${datadir_java}#g' \
        -e 's#@@JARFILENAME@@#${JARFILENAME}#g' \
        -e 's#@@libdir_jvm@@#${libdir_jvm}#g' \
        -e 's#@@sysconfdir@@#${sysconfdir}#g' \
        ${D}${sysconfdir}/ca-certificates/update.d/${BPN}-hook

    install -d -m0755 ${D}${sbindir}
    ln -s ${@os.path.relpath("${sysconfdir}/ca-certificates/update.d/${BPN}-hook", "${sbindir}")} \
          ${D}${sbindir}/create-ca-certificates-java
}

pkg_postinst:${PN} () {
    if [ -n "$D" ]; then
        # During the image build, generate the trustStore with the native
        # java from the image recipe's native sysroot. NATIVE_ROOT is needed
        # because STAGING_DIR_NATIVE points at this recipe's sysroot.
        if [ -z "${NATIVE_ROOT}" ]; then
            echo "$0: NATIVE_ROOT not known" >&2
            exit 1
        fi
        export JVM_LIBDIR="${NATIVE_ROOT}${libdir_native}/jvm"
    fi
    $D${sbindir}/create-ca-certificates-java
}

FILES:${PN} += "${datadir_java}"

RDEPENDS:${PN} = "ca-certificates"
RDEPENDS:${PN}:append:class-target = " openjdk"

BBCLASSEXTEND = "native"
