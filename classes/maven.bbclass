DEPENDS += "maven-bin-native"

MAVEN_JAVA_HOME = "${STAGING_DIR_NATIVE}/${libdir}/jvm"
MAVEN_HOME = "${STAGING_DIR_NATIVE}/${libdir}/maven"

MAVEN = "\
    JAVA_HOME="${MAVEN_JAVA_HOME}" \
    M2_HOME="${MAVEN_HOME}" \
    MAVEN_HOME="${MAVEN_HOME}" \
    ${MAVEN_HOME}/bin/mvn \
    --batch-mode \
    --strict-checksums \
    -T ${@oe.utils.parallel_make(d) or 1} \
"

MAVEN_BUILD_FLAGS ?= "clean package -DskipTests"
MAVEN_DEPENDENCY_PLUGIN ?= "org.apache.maven.plugins:maven-dependency-plugin:3.11.0"

def get_maven_debug_args(d):
    import bb.msg
    level = bb.msg.loggerDefaultLogLevel
    if level <= 9: # >= -DD
        return "-e -X"
    elif level <= 10: # -D
        return "-e"
    return ""

MAVEN_DEBUG_ARGS := "${@get_maven_debug_args(d)}"

# Maven using only the dependencies fetched from ${BPN}-deps.inc.
MAVEN_OFFLINE = "\
    ${MAVEN} \
    --offline \
    -Dmaven.repo.local=${DL_DIR}/maven-repo/${BP} \
    ${MAVEN_DEBUG_ARGS} \
"

do_compile() {
    cd ${S}
    ${MAVEN_OFFLINE} ${MAVEN_BUILD_FLAGS}
}

python do_cleanall:append() {
    import shutil
    mvn_repo_dir = f"{d.getVar('DL_DIR')}/maven-repo/{d.getVar('BP')}"
    if os.path.isdir(mvn_repo_dir):
        bb.debug(1, f"removing {mvn_repo_dir}")
        shutil.rmtree(mvn_repo_dir)
}
