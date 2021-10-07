package com.synopsys.integration.utility

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals

class BuildFileUtilityTest {
    private static String REMOTE_CGP_VERSION_CONTENTS = BuildFileUtility.getRemoteCgpVersionContents()
    private static File DUMMY_BUILD_SRC_BUILD_FILE = new File("/does/not/exist/build.gradle")

    private BuildFileUtility buildFileUtility = new BuildFileUtility()


    @Test
    void updateVersionSnapshotToQA() {
        // Verify inputBuildFile is updated with new qa Version
        String currentVersion = '1.0.1-SNAPSHOT'
        String newVersion = '3.13.39-SIGQA1'
        File inputBuildFile = createBuildFile("", "version = '${currentVersion}'")
        File expectedContents = createBuildFile("", "version = '${newVersion}'")

        buildFileUtility.updateBuildScriptVersion(inputBuildFile, newVersion, 'qa')
        assertEquals(expectedContents.text, inputBuildFile.text)
    }

    @Test
    void updateVersionSnapshotToRelease() {
        // Verify inputBuildFile is updated with new release Version
        String currentVersion = '1.0.1-SNAPSHOT'
        String newVersion = '3.13.39'
        File inputBuildFile = createBuildFile("", "version = '${currentVersion}'")
        File expectedContents = createBuildFile("", "version = '${newVersion}'")

        buildFileUtility.updateBuildScriptVersion(inputBuildFile, newVersion, 'release')
        assertEquals(expectedContents.text, inputBuildFile.text)
    }

    @Test
    void updateVersionQAToQA() {
        // Verify inputBuildFile is updated with new qa Version
        String currentVersion = '1.0.1-SIGQA1'
        String newVersion = '3.13.39-SIGQA2'
        File inputBuildFile = createBuildFile("", "version = '${currentVersion}'")
        File expectedContents = createBuildFile("", "version = '${newVersion}'")

        buildFileUtility.updateBuildScriptVersion(inputBuildFile, newVersion, 'qa')
        assertEquals(expectedContents.text, inputBuildFile.text)
    }

    @Test
    void updateVersionQAToRelease() {
        // Verify inputBuildFile is updated with new release Version
        String currentVersion = '1.0.1-SIGQA1'
        String newVersion = '3.13.39'
        File inputBuildFile = createBuildFile("", "version = '${currentVersion}'")
        File expectedContents = createBuildFile("", "version = '${newVersion}'")

        buildFileUtility.updateBuildScriptVersion(inputBuildFile, newVersion, 'release')
        assertEquals(expectedContents.text, inputBuildFile.text)
    }

    @Test
    void updateVersionReleaseToSnapshot() {
        // Verify inputBuildFile is updated with new snapshot Version
        String currentVersion = '1.0.1'
        String newVersion = '3.13.39-SNAPSHOT'
        File inputBuildFile = createBuildFile("", "version = '${currentVersion}'")
        File expectedContents = createBuildFile("", "version = '${newVersion}'")

        buildFileUtility.updateBuildScriptVersion(inputBuildFile, newVersion, 'release')
        assertEquals(expectedContents.text, inputBuildFile.text)
    }

    @Test
    void updateVersionNoUpdates() {
        // Verify inputBuildFile is not changed
        String newVersion = '3.13.39-SNAPSHOT'
        File inputBuildFile = createBuildFile("", "")
        String expectedContents = inputBuildFile.text

        buildFileUtility.updateBuildScriptVersion(inputBuildFile, newVersion, 'release')
        assertEquals(expectedContents, inputBuildFile.text)
    }

    @Test
    void updateVersionMultipleVersionLines() {
        // Ensure version listed in subprojects is not changed
        String subprojectConfigurations = """
                                         
                                          subprojects {
                                              group = rootProject.group
                                              version = rootProject.version
                                              apply plugin: 'com.synopsys.integration.simple'
                                          }
                                          """

        String currentVersion = '1.0.1'
        String newVersion = '3.13.39-SNAPSHOT'
        File inputBuildFile = createBuildFile("", "version = '${currentVersion}'${subprojectConfigurations}")
        File expectedContents = createBuildFile("", "version = '${newVersion}'${subprojectConfigurations}")

        buildFileUtility.updateBuildScriptVersion(inputBuildFile, newVersion, 'release')
        assertEquals(expectedContents.text, inputBuildFile.text)
    }

    @Test
    void updateBuildScriptRootOnlyToRemote() {
        // Verify rootProjectBuildFile was changed from 'apply from' to remote content
        File rootProjectBuildFile = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        String expectedContents = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "").text
        buildFileUtility.updateBuildScriptForRelease(DUMMY_BUILD_SRC_BUILD_FILE, rootProjectBuildFile)

        assertEquals(expectedContents, rootProjectBuildFile.text)
        assertNotEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
    }

    @Test
    void updateBuildScriptRootOnlyFromRemote() {
        // Verify rootProjectBuildFile was changed from remote content to 'apply from'
        File rootProjectBuildFile = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        String expectedContents = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "").text
        buildFileUtility.updateBuildScriptForSnapshot(DUMMY_BUILD_SRC_BUILD_FILE, rootProjectBuildFile)

        assertEquals(expectedContents, rootProjectBuildFile.text)
        assertNotEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
    }

    @Test
    void updateBuildScriptRootOnlyNoChange() {
        // Verify no changes were made to rootProjectBuildFile
        File rootProjectBuildFile = createBuildFile("Does Not Exist", "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        String expectedContents = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "").text
        buildFileUtility.updateBuildScriptForSnapshot(DUMMY_BUILD_SRC_BUILD_FILE, rootProjectBuildFile)

        assertNotEquals(expectedContents, rootProjectBuildFile.text)
        assertEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
    }

    @Test
    void updateBuildScriptApplyContentInBuildSrc() {
        // Verify rootProjectBuildFile did not change, and buildSrcBuildFile was updated with remote contents
        File rootProjectBuildFile = createBuildFile("", "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        File buildSrcBuildFile = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "")
        String originalBuildSrcBuildFileContents = buildSrcBuildFile.text
        String expectedContents = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "").text
        buildFileUtility.updateBuildScriptForRelease(buildSrcBuildFile, rootProjectBuildFile)

        assertEquals(expectedContents, buildSrcBuildFile.text)
        assertEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
        assertNotEquals(originalBuildSrcBuildFileContents, buildSrcBuildFile.text)
    }

    @Test
    void updateBuildScriptApplyContentInRoot() {
        // Verify rootProjectBuildFile was updated with remote contents, and buildSrcBuildFile did not change
        File rootProjectBuildFile = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        File buildSrcBuildFile = createBuildFile("", "")
        String originalBuildSrcBuildFileContents = buildSrcBuildFile.text
        String expectedContents = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "").text
        buildFileUtility.updateBuildScriptForRelease(buildSrcBuildFile, rootProjectBuildFile)

        assertEquals(expectedContents, rootProjectBuildFile.text)
        assertNotEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
        assertEquals(originalBuildSrcBuildFileContents, buildSrcBuildFile.text)
    }

    @Test
    void updateBuildScriptApplyContentInBoth() {
        // Verify rootProjectBuildFile did not change, and buildSrcBuildFile was updated with remote contents
        File rootProjectBuildFile = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        File buildSrcBuildFile = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "")
        String originalBuildSrcBuildFileContents = buildSrcBuildFile.text
        String expectedContents = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "").text
        buildFileUtility.updateBuildScriptForRelease(buildSrcBuildFile, rootProjectBuildFile)

        assertEquals(expectedContents, buildSrcBuildFile.text)
        assertEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
        assertNotEquals(originalBuildSrcBuildFileContents, buildSrcBuildFile.text)
    }

    @Test
    void updateBuildScriptRemoteContentInBuildSrc() {
        // Verify rootProjectBuildFile did not change, and buildSrcBuildFile was updated with apply from
        File rootProjectBuildFile = createBuildFile("", "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        File buildSrcBuildFile = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "")
        String originalBuildSrcBuildFileContents = buildSrcBuildFile.text
        String expectedContents = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "").text
        buildFileUtility.updateBuildScriptForSnapshot(buildSrcBuildFile, rootProjectBuildFile)

        assertEquals(expectedContents, buildSrcBuildFile.text)
        assertEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
        assertNotEquals(originalBuildSrcBuildFileContents, buildSrcBuildFile.text)
    }

    @Test
    void updateBuildScriptRemoteContentInRoot() {
        // Verify rootProjectBuildFile was updated with apply from, and buildSrcBuildFile did not change
        File rootProjectBuildFile = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        File buildSrcBuildFile = createBuildFile("", "")
        String originalBuildSrcBuildFileContents = buildSrcBuildFile.text
        String expectedContents = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "").text
        buildFileUtility.updateBuildScriptForSnapshot(buildSrcBuildFile, rootProjectBuildFile)

        assertEquals(expectedContents, rootProjectBuildFile.text)
        assertNotEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
        assertEquals(originalBuildSrcBuildFileContents, buildSrcBuildFile.text)
    }

    @Test
    void updateBuildScriptRemoteContentInBoth() {
        // Verify rootProjectBuildFile did not change, and buildSrcBuildFile was updated with apply from
        File rootProjectBuildFile = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        File buildSrcBuildFile = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "")
        String originalBuildSrcBuildFileContents = buildSrcBuildFile.text
        String expectedContents = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "").text
        buildFileUtility.updateBuildScriptForSnapshot(buildSrcBuildFile, rootProjectBuildFile)

        assertEquals(expectedContents, buildSrcBuildFile.text)
        assertEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
        assertNotEquals(originalBuildSrcBuildFileContents, buildSrcBuildFile.text)
    }

    @Test
    void updateBuildScriptContentInNeither() {
        // Verify rootProjectBuildFile did not change, and buildSrcBuildFile did not change
        File rootProjectBuildFile = createBuildFile("", "")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        File buildSrcBuildFile = createBuildFile("", "")
        String originalBuildSrcBuildFileContents = buildSrcBuildFile.text
        buildFileUtility.updateBuildScriptForSnapshot(buildSrcBuildFile, rootProjectBuildFile)

        assertEquals(originalBuildSrcBuildFileContents, buildSrcBuildFile.text)
        assertEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
    }

    @Test
    void productionTestSnapshotToRelease() {
        // Verify rootProjectBuildFile updated version, and buildSrcBuildFile updated to remote content
        String currentVersion = '1.0.1-SNAPSHOT'
        String newVersion = '3.13.1521'
        File rootProjectBuildFile = createBuildFile("", "version = '${currentVersion}'")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        File expectedRootProjectBuildFile = createBuildFile("", "version = '${newVersion}'")
        File buildSrcBuildFile = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "")
        String originalBuildSrcBuildFileContents = buildSrcBuildFile.text
        File expectedBuildSrcBuildFile = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "")

        buildFileUtility.updateBuildScriptForRelease(buildSrcBuildFile, rootProjectBuildFile)
        buildFileUtility.updateBuildScriptVersion(rootProjectBuildFile, newVersion, 'release')

        assertEquals(expectedRootProjectBuildFile.text, rootProjectBuildFile.text)
        assertNotEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
        assertEquals(expectedBuildSrcBuildFile.text, buildSrcBuildFile.text)
        assertNotEquals(originalBuildSrcBuildFileContents, buildSrcBuildFile.text)
    }

    @Test
    void productionTestReleaseToSnapshot() {
        // Verify rootProjectBuildFile updated version, and buildSrcBuildFile updated to apply from
        String currentVersion = '3.13.1521'
        String newVersion = '1.0.1-SNAPSHOT'
        File rootProjectBuildFile = createBuildFile("", "version = '${currentVersion}'")
        String originalRootProjectBuildFileContents = rootProjectBuildFile.text
        File expectedRootProjectBuildFile = createBuildFile("", "version = '${newVersion}'")
        File buildSrcBuildFile = createBuildFile(REMOTE_CGP_VERSION_CONTENTS, "")
        String originalBuildSrcBuildFileContents = buildSrcBuildFile.text
        File expectedBuildSrcBuildFile = createBuildFile(BuildFileUtility.CGP_VERSION_APPLY_FROM_LINE, "")

        buildFileUtility.updateBuildScriptForSnapshot(buildSrcBuildFile, rootProjectBuildFile)
        buildFileUtility.updateBuildScriptVersion(rootProjectBuildFile, newVersion, 'release')

        assertEquals(expectedRootProjectBuildFile.text, rootProjectBuildFile.text)
        assertNotEquals(originalRootProjectBuildFileContents, rootProjectBuildFile.text)
        assertEquals(expectedBuildSrcBuildFile.text, buildSrcBuildFile.text)
        assertNotEquals(originalBuildSrcBuildFileContents, buildSrcBuildFile.text)
    }

    private static File createTempFile(String contents) {
        File buildFile = File.createTempFile('test_build', '.gradle', new File('build'))
        buildFile.deleteOnExit()
        buildFile << """
                    ${contents}
               """
        return buildFile
    }

    private static File createBuildFile(String cgpVersionEntry, String versionString) {
        String buildFileContent = """
                    buildscript {
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-repositories.gradle", to: buildscript
                        ${cgpVersionEntry}
                        dependencies { classpath "com.synopsys.integration:common-gradle-plugin:\${managedCgpVersion}" }
                    }

                    group 'com.synopsys.integration'
                    ${versionString}
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """

        return createTempFile(buildFileContent)
    }
}
