#
# SPDX-License-Identifier: MIT
#

import os
import re

from oeqa.core.decorator.data import skipIfNotDataVar
from oeqa.core.decorator.depends import OETestDepends
from oeqa.runtime.case import OERuntimeTestCase
from oeqa.runtime.decorator.package import OEHasPackage

LAYER_DIR = os.path.join(os.path.dirname(os.path.realpath(__file__)), "..", "..", "..", "..")
# The same source openjdk-test-jar builds on the host for images without javac.
TEST_SOURCE = os.path.join(LAYER_DIR, "recipes-test", "openjdk-test-jar", "files", "TheAnswer.java")
EXPECTED_OUTPUT = "the answer is 42"


def run_java(case, args):
    """Run java with args on the target and return its output."""
    status, output = case.target.run(f"java {args}")
    case.assertEqual(status, 0, msg=f"java {args} failed: {output}")
    return output


def check_program(case, program):
    """Check the test program runs in the default, interpreted and compiled modes."""
    case.assertEqual(run_java(case, program), EXPECTED_OUTPUT)

    output = run_java(case, f"-showversion -Xint {program}")
    case.assertIn("interpreted mode", output)
    case.assertIn(EXPECTED_OUTPUT, output)

    # The Zero VM has no JIT compiler.
    if "Zero VM" not in output:
        output = run_java(case, f"-showversion -Xcomp {program}")
        case.assertIn("compiled mode", output)
        case.assertIn(EXPECTED_OUTPUT, output)


class OpenJDKTest(OERuntimeTestCase):
    @OETestDepends(["ssh.SSHTest.test_ssh"])
    @OEHasPackage(["openjdk"])
    def test_java_version(self):
        output = run_java(self, "-version")
        versions = re.findall(r'version "(\d+)', output)
        self.assertTrue(versions, msg=f"No version in: {output}")
        self.assertEqual(versions[0], self.td["OPENJDK_TEST_VERSION"], msg=output)

    @OETestDepends(["openjdk.OpenJDKTest.test_java_version"])
    @OEHasPackage(["ca-certificates-java"])
    def test_cacerts(self):
        cacerts = os.path.join(self.td["sysconfdir"], "ssl", "certs", "java", "cacerts")
        status, output = self.target.run(f"keytool -list -keystore {cacerts} -storepass changeit")
        self.assertEqual(status, 0, msg=output)
        entries = re.findall(r"Your keystore contains (\d+) entr", output)
        self.assertTrue(entries, msg=output)
        self.assertGreater(int(entries[0]), 0, msg=output)


class JavaJarTest(OERuntimeTestCase):
    """The test program built on the host, for images without javac."""

    @OETestDepends(["openjdk.OpenJDKTest.test_java_version"])
    @OEHasPackage(["openjdk-test-jar"])
    def test_jar(self):
        jar = os.path.join(self.td["datadir"], "java", "openjdk-test-jar.jar")
        check_program(self, f"-jar {jar}")


class JavacTest(OERuntimeTestCase):
    """The test program compiled on the target, for images with a full JDK."""

    workdir = "/tmp/openjdk-test"

    @classmethod
    def tearDownClass(cls):
        cls.tc.target.run(f"rm -rf {cls.workdir}")

    @skipIfNotDataVar("OPENJDK_TEST_JDK", "1", "Requires a full JDK (openjdk-test-image)")
    @OETestDepends(["openjdk.OpenJDKTest.test_java_version"])
    def test_javac(self):
        self.target.run(f"mkdir -p {self.workdir}")
        self.target.copyTo(TEST_SOURCE, self.workdir)
        status, output = self.target.run(f"javac -d {self.workdir} {self.workdir}/TheAnswer.java")
        self.assertEqual(status, 0, msg=output)

    @OETestDepends(["openjdk.JavacTest.test_javac"])
    def test_compiled_program(self):
        check_program(self, f"-cp {self.workdir} TheAnswer")
