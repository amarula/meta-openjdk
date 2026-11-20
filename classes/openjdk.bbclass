def get_openjdk_build_version(d):
    pv = bb.parse.vars_from_file(d.getVar('FILE', False),d)[1]
    return pv.split("+")[1]

def get_openjdk_version(d):
    supported_versions = ["27", "25", "21", "17"]
    preferred_version = d.getVar("PREFERRED_VERSION_openjdk")

    if preferred_version is None:
        return supported_versions[0]

    if "%" in preferred_version:
        preferred_version = preferred_version.split("%")[0]

    preferred_version = preferred_version.split(".")[0]
    if preferred_version not in supported_versions:
        bb.fatal(f"Unsupported PREFERRED_VERSION_openjdk: {preferred_version}")

    return preferred_version

def openjdk_version_at_least(d, version):
    """True when the selected OpenJDK major version is version or newer."""
    return int(get_openjdk_version(d)) >= int(version)

# Restrict which OpenJDK major versions a recipe can be built with.
# Both are optional and may be combined:
#   OPENJDK_MIN_VERSION = "25"             25 or newer
#   OPENJDK_SUPPORTED_VERSIONS = "21,25"   only 21 or 25
OPENJDK_MIN_VERSION ??= ""
OPENJDK_SUPPORTED_VERSIONS ??= ""

# This class is inherited globally through sanity_meta_openjdk.bbclass, so the
# task is only added by classes that need it (see jar.bbclass).
python do_check_openjdk_version() {
    pn = d.getVar("PN")
    selected = get_openjdk_version(d)
    hint = "Change PREFERRED_VERSION_openjdk to a compatible version."

    min_version = d.getVar("OPENJDK_MIN_VERSION")
    if min_version and not openjdk_version_at_least(d, min_version):
        bb.fatal(f"{pn} requires OpenJDK {min_version} or newer, "
                 f"but OpenJDK {selected} is selected. {hint}")

    supported = [v.strip() for v in (d.getVar("OPENJDK_SUPPORTED_VERSIONS") or "").split(",") if v.strip()]
    if supported and selected not in supported:
        bb.fatal(f"{pn} supports OpenJDK {', '.join(supported)}, "
                 f"but OpenJDK {selected} is selected. {hint}")
}
do_check_openjdk_version[doc] = "Ensure the selected OpenJDK matches OPENJDK_MIN_VERSION and OPENJDK_SUPPORTED_VERSIONS"
