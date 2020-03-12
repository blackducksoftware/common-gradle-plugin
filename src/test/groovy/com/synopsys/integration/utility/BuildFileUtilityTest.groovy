package com.synopsys.integration.utility

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class BuildFileUtilityTest {
    @Test
    void updateVersionContentTest() {
        String buildFileContent = """
                    buildscript {
                        repositories {
                            mavenLocal()
                            jcenter()
                            mavenCentral()
                            maven { url 'https://plugins.gradle.org/m2/' }
                        }
                        dependencies { classpath "com.synopsys.integration:common-gradle-plugin:1.0.1-SNAPSHOT" }
                    }

                    group 'com.synopsys.integration'
                    version = '1.0.1-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String buildFileContentExpected = """
                    buildscript {
                        repositories {
                            mavenLocal()
                            jcenter()
                            mavenCentral()
                            maven { url 'https://plugins.gradle.org/m2/' }
                        }
                        dependencies { classpath "com.synopsys.integration:common-gradle-plugin:1.0.1-SNAPSHOT" }
                    }

                    group 'com.synopsys.integration'
                    version = '23.45.765-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String currentVersion = '1.0.1-SNAPSHOT'
        String newVersion = '23.45.765-SNAPSHOT'

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        String newBuildFileContent = buildFileUtility.updateVersion(buildFileContent, currentVersion, newVersion)

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateVersionContentSimpleTest() {
        String buildFileContent = """
                    version = '1.0.1-SNAPSHOT'
                """
        String buildFileContentExpected = """
                    version = '23.45.765-SNAPSHOT'
                """
        String currentVersion = '1.0.1-SNAPSHOT'
        String newVersion = '23.45.765-SNAPSHOT'

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        String newBuildFileContent = buildFileUtility.updateVersion(buildFileContent, currentVersion, newVersion)

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateVersionContentQAVersionTest() {
        String buildFileContent = """
                    version = '1.0.1-SNAPSHOT'
                """
        String buildFileContentExpected = """
                    version = '23.45.765-SIGQA1'
                """
        String currentVersion = '1.0.1-SNAPSHOT'
        String newVersion = '23.45.765-SIGQA1'

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        String newBuildFileContent = buildFileUtility.updateVersion(buildFileContent, currentVersion, newVersion)

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateVersionContentQAVersionToSNAPSHOTTest() {
        String buildFileContent = """
                    version = '1.0.1-SIGQA369'
                """
        String buildFileContentExpected = """
                    version = '23.45.765-SIGQA789709-SNAPSHOT'
                """
        String currentVersion = '1.0.1-SIGQA369'
        String newVersion = '23.45.765-SIGQA789709-SNAPSHOT'

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        String newBuildFileContent = buildFileUtility.updateVersion(buildFileContent, currentVersion, newVersion)

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateVersionContentQASNAPSHOTToReleaseTest() {
        String buildFileContent = """
                    version = '1.0.1-SIGQA369-SNAPSHOT'
                """
        String buildFileContentExpected = """
                    version = '23.45.765'
                """
        String currentVersion = '1.0.1-SIGQA369-SNAPSHOT'
        String newVersion = '23.45.765'

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        String newBuildFileContent = buildFileUtility.updateVersion(buildFileContent, currentVersion, newVersion)

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateVersionContentReleaseToSNAPSHOTTest() {
        String buildFileContent = """
                    version = '1.0.1'
                """
        String buildFileContentExpected = """
                    version = '23.45.765-SNAPSHOT'
                """
        String currentVersion = '1.0.1'
        String newVersion = '23.45.765-SNAPSHOT'

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        String newBuildFileContent = buildFileUtility.updateVersion(buildFileContent, currentVersion, newVersion)

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateVersionContentDoubleVersionTest() {
        String buildFileContent = """
                    version = '1.0.1-SNAPSHOT'
                    description = "version = '1.0.1-SNAPSHOT'"
                """
        String buildFileContentExpected = """
                    version = '23.45.765-SNAPSHOT'
                    description = "version = '23.45.765-SNAPSHOT'"
                """
        String currentVersion = '1.0.1-SNAPSHOT'
        String newVersion = '23.45.765-SNAPSHOT'

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        String newBuildFileContent = buildFileUtility.updateVersion(buildFileContent, currentVersion, newVersion)

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateVersionContentByFileTest() {
        File buildFile = File.createTempFile('test_build', '.gradle', new File('build'))
        buildFile.deleteOnExit()
        buildFile << """
                    buildscript {
                        repositories {
                            mavenLocal()
                            jcenter()
                            mavenCentral()
                            maven { url 'https://plugins.gradle.org/m2/' }
                        }
                        dependencies { classpath "com.synopsys.integration:common-gradle-plugin:1.0.1-SNAPSHOT" }
                    }

                    group 'com.synopsys.integration'
                    version = "1.0.1-SNAPSHOT"
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String buildFileContentExpected = """
                    buildscript {
                        repositories {
                            mavenLocal()
                            jcenter()
                            mavenCentral()
                            maven { url 'https://plugins.gradle.org/m2/' }
                        }
                        dependencies { classpath "com.synopsys.integration:common-gradle-plugin:1.0.1-SNAPSHOT" }
                    }

                    group 'com.synopsys.integration'
                    version = '23.45.765-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String currentVersion = '1.0.1-SNAPSHOT'
        String newVersion = '23.45.765-SNAPSHOT'

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        buildFileUtility.updateVersion(buildFile, currentVersion, newVersion)
        String newBuildFileContent = buildFile.text

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateBuildScriptDependenciesToApplyFromRemoteTest() {
        String buildFileContent = """
                    buildscript {
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-repositories.gradle", to: buildscript
                        ////////// START BUILDSCRIPT DEPENDENCY //////////
                        dependencies { classpath "com.synopsys.integration:common-gradle-plugin:1.3.2-SNAPSHOT" }
                        
                        ////////// END BUILDSCRIPT DEPENDENCY //////////
                    }

                    group 'com.synopsys.integration'
                    version = '1.0.1-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String buildFileContentExpected = """
                    buildscript {
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-repositories.gradle", to: buildscript
                        apply from: 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle', to: buildscript
                    }

                    group 'com.synopsys.integration'
                    version = '1.0.1-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String newContent = "apply from: 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle', to: buildscript"

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        String newBuildFileContent = buildFileUtility.updateBuildScriptDependenciesToApplyFromRemote(buildFileContent, newContent)

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateBuildScriptDependenciesToApplyFromRemoteFileTest() {
        File buildFile = File.createTempFile('test_build', '.gradle', new File('build'))
        buildFile.deleteOnExit()
        buildFile << """
                    buildscript {
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-repositories.gradle", to: buildscript
                        ////////// START BUILDSCRIPT DEPENDENCY //////////
                        dependencies { classpath "com.synopsys.integration:common-gradle-plugin:1.3.2-SNAPSHOT" }
                        
                        ////////// END BUILDSCRIPT DEPENDENCY //////////
                    }

                    group 'com.synopsys.integration'
                    version = '1.0.1-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String buildFileContentExpected = """
                    buildscript {
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-repositories.gradle", to: buildscript
                        apply from: 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle', to: buildscript
                    }

                    group 'com.synopsys.integration'
                    version = '1.0.1-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String newContent = "apply from: 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle', to: buildscript"

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        buildFileUtility.updateBuildScriptDependenciesToApplyFromRemote(buildFile, newContent)
        String newBuildFileContent = buildFile.text

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }


    @Test
    void updateBuildScriptDependenciesToRemoteContentTest() {
        String buildFileContent = """
                    buildscript {
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-repositories.gradle", to: buildscript
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle", to: buildscript
                    }

                    group 'com.synopsys.integration'
                    version = '1.0.1-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String buildFileContentExpected = """
                    buildscript {
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-repositories.gradle", to: buildscript
                        ////////// START BUILDSCRIPT DEPENDENCY //////////
test content
////////// END BUILDSCRIPT DEPENDENCY //////////
                    }

                    group 'com.synopsys.integration'
                    version = '1.0.1-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String newContent = "test content"

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        String newBuildFileContent = buildFileUtility.updateBuildScriptDependenciesToRemoteContent(buildFileContent, newContent)

        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

    @Test
    void updateBuildScriptDependenciesToRemoteContentFileTest() {
        File buildFile = File.createTempFile('test_build', '.gradle', new File('build'))
        buildFile.deleteOnExit()
        buildFile << """
                    buildscript {
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-repositories.gradle", to: buildscript
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle", to: buildscript
                    }

                    group 'com.synopsys.integration'
                    version = '1.0.1-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String buildFileContentExpected = """
                    buildscript {
                        apply from: "https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-repositories.gradle", to: buildscript
                        ////////// START BUILDSCRIPT DEPENDENCY //////////
test content
////////// END BUILDSCRIPT DEPENDENCY //////////
                    }

                    group 'com.synopsys.integration'
                    version = '1.0.1-SNAPSHOT'
                    apply plugin: 'com.synopsys.integration.library'
                    dependencies {
                        implementation "log4j:log4j:1.0.1-SNAPSHOT"
                        testCompileOnly group: 'log4j', name: 'log4j', version: '1.0.1-SNAPSHOT'
                    }
                """
        String newContent = "test content"

        BuildFileUtility buildFileUtility = new BuildFileUtility()
        buildFileUtility.updateBuildScriptDependenciesToRemoteContent(buildFile, newContent)
        String newBuildFileContent = buildFile.text
        assertEquals(buildFileContentExpected, newBuildFileContent)
    }

}
