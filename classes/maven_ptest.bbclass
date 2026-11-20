# Package the upstream JUnit tests of a Maven project as a ptest.
#
# do_maven_update_deps sets JUNIT_CONSOLE_VERSION in ${BPN}-deps.inc to the
# junit-platform version on the project's test classpath, and fetches the
# matching JUnit console launcher.
#
# Tests run against the installed ${datadir_java}/${BPN}.jar.
# Extra arguments for the console launcher (e.g. --exclude-classname)
# can be passed with JUNIT_CONSOLE_ARGS.

inherit maven maven_update_deps ptest

JUNIT_CONSOLE_ARGS ??= ""
MAVEN_PTEST_RUNNER = "${@bb.utils.which(d.getVar('BBPATH'), 'files/maven-ptest/run-ptest.in')}"
JUNIT_CONSOLE_ARTIFACT = "org.junit.platform:junit-platform-console-standalone:${JUNIT_CONSOLE_VERSION}"

def maven_ptest_fetch_junit_console(d, maven_repo, extra_vars):
    """Called by do_maven_update_deps. Fetch the JUnit console launcher
    matching the junit-platform version on the project's test classpath."""
    import os
    import re

    plugin = d.getVar("MAVEN_DEPENDENCY_PLUGIN")
    listing = os.path.join(d.getVar("WORKDIR"), "junit-platform.list")
    urls = maven_update_deps_run(
        d, maven_repo,
        f"{plugin}:list",
        "-DincludeScope=test",
        "-DincludeGroupIds=org.junit.platform",
        "-DincludeArtifactIds=junit-platform-engine",
        f"-DoutputFile={listing}",
    )
    with open(listing) as f:
        versions = re.findall(r"org\.junit\.platform:junit-platform-engine:jar:([^:\s]+)", f.read())
    if not versions:
        bb.fatal("junit-platform-engine is not on the test classpath")

    version = versions[0]
    extra_vars["JUNIT_CONSOLE_VERSION"] = version
    return urls + maven_update_deps_run(
        d, maven_repo,
        f"{plugin}:get",
        "-Dtransitive=false",
        f"-Dartifact=org.junit.platform:junit-platform-console-standalone:{version}",
    )

# Both goals use -DoutputDirectory, which Maven applies globally,
# so they have to run separately.
do_compile_ptest() {
    cd ${S}
    ${MAVEN_OFFLINE} \
        ${MAVEN_DEPENDENCY_PLUGIN}:copy-dependencies \
        -DincludeScope=test \
        -DexcludeGroupIds=org.junit.platform,org.junit.jupiter,org.junit.vintage \
        -DoutputDirectory=target/ptest/lib

    ${MAVEN_OFFLINE} \
        ${MAVEN_DEPENDENCY_PLUGIN}:copy \
        -Dartifact=${JUNIT_CONSOLE_ARTIFACT} \
        -DoutputDirectory=target/ptest
}
do_compile_ptest_base[cleandirs] = "${S}/target/ptest"

do_install_ptest() {
    cd ${S}/target
    find test-classes -type f -exec install -D -m 0644 {} ${D}${PTEST_PATH}/{} \;
    install -d ${D}${PTEST_PATH}/lib
    install -m 0644 ptest/lib/*.jar ${D}${PTEST_PATH}/lib/
    install -m 0644 ptest/junit-platform-console-standalone-${JUNIT_CONSOLE_VERSION}.jar \
        ${D}${PTEST_PATH}/junit-console.jar

    # Some tests open their resources with paths relative to the source tree.
    # Maven copies src/test/resources into test-classes.
    install -d ${D}${PTEST_PATH}/src/test
    ln -s ../../test-classes ${D}${PTEST_PATH}/src/test/resources

    install -m 0755 ${MAVEN_PTEST_RUNNER} ${D}${PTEST_PATH}/run-ptest
    sed -i \
        -e 's#@@BPN@@#${BPN}#g' \
        -e 's#@@datadir_java@@#${datadir_java}#g' \
        -e 's#@@JUNIT_CONSOLE_ARGS@@#${JUNIT_CONSOLE_ARGS}#g' \
        ${D}${PTEST_PATH}/run-ptest
}
do_install_ptest_base[file-checksums] += "${MAVEN_PTEST_RUNNER}:True"
