/*
 * common-gradle-plugin
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration

import com.synopsys.integration.utility.BuildFileUtility
import com.synopsys.integration.utility.VersionUtility
import org.cadixdev.gradle.licenser.LicenseExtension
import org.cadixdev.gradle.licenser.Licenser
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin

import java.nio.charset.StandardCharsets
import java.util.jar.Manifest

abstract class Common implements Plugin<Project> {
    public static final String HEADER_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/HEADER.txt'
    public static final String EULA_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/EULA.txt'
    public static final String LICENSE_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/LICENSE'
    public static final String GIT_IGNORE_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/.gitignore'
    public static final String README_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/README.md'
    public static final String BUILDSCRIPT_DEPENDENCY_LOCATION = 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle'

    public static final String HEADER_NAME = 'HEADER.txt'

    public static final String PROPERTY_DEPLOY_ARTIFACTORY_URL = 'deployArtifactoryUrl'
    public static final String PROPERTY_DOWNLOAD_ARTIFACTORY_URL = 'downloadArtifactoryUrl'
    public static final String PROPERTY_ARTIFACTORY_SNAPSHOT_REPO = 'artifactoryRepo'
    public static final String PROPERTY_ARTIFACTORY_RELEASE_REPO = 'artifactoryReleaseRepo'
    public static final String PROPERTY_JUNIT_PLATFORM_DEFAULT_TEST_TAGS = 'junitPlatformDefaultTestTags'
    public static final String PROPERTY_JUNIT_PLATFORM_CUSTOM_TEST_TAGS = 'junitPlatformCustomTestTags'
    public static final String PROPERTY_JUNIT_SHOW_STANDARD_STREAMS = 'junitShowStandardStreams'
    public static final String PROPERTY_JAVA_SOURCE_COMPATIBILITY = 'javaSourceCompatibility'
    public static final String PROPERTY_JAVA_TARGET_COMPATIBILITY = 'javaTargetCompatibility'
    public static final String PROPERTY_JAVA_USE_AUTO_MODULE_NAME = 'javaUseAutoModuleName'
    public static final String PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_HEADER = 'synopsysOverrideIntegrationHeader'
    public static final String PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_EULA = 'synopsysOverrideIntegrationEula'
    public static final String PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_LICENSE = 'synopsysOverrideIntegrationLicense'
    public static final String PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_GIT_IGNORE = 'synopsysOverrideIntegrationGitIgnore'
    public static final String PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_README = 'synopsysOverrideIntegrationReadme'
    public static final String PROPERTY_BUILDSCRIPT_DEPENDENCY = 'buildscriptDependency'
    public static final String PROPERTY_EXCLUDES_FROM_TEST_COVERAGE = 'excludesFromTestCoverage'

    public static final String PROPERTY_ARTIFACTORY_ARTIFACT_NAME = 'artifactoryArtifactName'
    public static final String PROPERTY_ARTIFACTORY_DEPLOYER_USERNAME = 'artifactoryDeployerUsername'
    public static final String PROPERTY_ARTIFACTORY_DEPLOYER_PASSWORD = 'artifactoryDeployerPassword'
    public static final String ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_USERNAME = 'ARTIFACTORY_DEPLOYER_USER'
    public static final String ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_PASSWORD = 'ARTIFACTORY_DEPLOYER_PASSWORD'

    public static final String PROPERTY_SONATYPE_USERNAME = 'sonatypeUsername'
    public static final String PROPERTY_SONATYPE_PASSWORD = 'sonatypePassword'
    public static final String ENVIRONMENT_VARIABLE_SONATYPE_USERNAME = 'SONATYPE_USERNAME'
    public static final String ENVIRONMENT_VARIABLE_SONATYPE_PASSWORD = 'SONATYPE_PASSWORD'

    public static final String PROPERTY_TEST_TAGS_TO_INCLUDE = 'tags'

    public static final Set<String> ALL_TEST_TAG = ['all'] as Set

    private Project project

    void apply(Project project) {
        this.project = project

        displayApplyMessage()

        project.ext[PROPERTY_BUILDSCRIPT_DEPENDENCY] = BUILDSCRIPT_DEPENDENCY_LOCATION

        // assume some reasonable defaults if the environment doesn't provide specific values
        setExtPropertyOnProject(PROPERTY_DOWNLOAD_ARTIFACTORY_URL, 'https://sig-repo.synopsys.com')
        setExtPropertyOnProject(PROPERTY_ARTIFACTORY_SNAPSHOT_REPO, 'bds-integrations-snapshot')
        setExtPropertyOnProject(PROPERTY_ARTIFACTORY_RELEASE_REPO, 'bds-integrations-release')
        setExtPropertyOnProject(PROPERTY_JUNIT_PLATFORM_DEFAULT_TEST_TAGS, 'integration, performance')
        setExtPropertyOnProject(PROPERTY_JUNIT_PLATFORM_CUSTOM_TEST_TAGS, '')
        setExtPropertyOnProject(PROPERTY_JUNIT_SHOW_STANDARD_STREAMS, 'false')
        setExtPropertyOnProject(PROPERTY_JAVA_SOURCE_COMPATIBILITY, JavaVersion.VERSION_11.toString())
        setExtPropertyOnProject(PROPERTY_JAVA_TARGET_COMPATIBILITY, JavaVersion.VERSION_11.toString())
        setExtPropertyOnProject(PROPERTY_JAVA_USE_AUTO_MODULE_NAME, 'true')
        setExtPropertyOnProject(PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_HEADER, 'false')
        setExtPropertyOnProject(PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_EULA, 'false')
        setExtPropertyOnProject(PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_LICENSE, 'false')
        setExtPropertyOnProject(PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_GIT_IGNORE, 'true')
        setExtPropertyOnProject(PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_README, 'true')

        setExtPropertyOnProject(PROPERTY_TEST_TAGS_TO_INCLUDE, '')

        // By default we should not exclude anything
        setExtPropertyOnProject(PROPERTY_EXCLUDES_FROM_TEST_COVERAGE, '')

        // there is no default public artifactory for deploying
        setExtPropertyOnProject(PROPERTY_DEPLOY_ARTIFACTORY_URL, '')

        // can't assume anything here because passwords have no reasonable defaults
        setExtPropertyOnProjectNoDefaults(PROPERTY_ARTIFACTORY_DEPLOYER_USERNAME, ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_USERNAME)
        setExtPropertyOnProjectNoDefaults(PROPERTY_ARTIFACTORY_DEPLOYER_PASSWORD, ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_PASSWORD)
        setExtPropertyOnProjectNoDefaults(PROPERTY_SONATYPE_USERNAME, ENVIRONMENT_VARIABLE_SONATYPE_USERNAME)
        setExtPropertyOnProjectNoDefaults(PROPERTY_SONATYPE_PASSWORD, ENVIRONMENT_VARIABLE_SONATYPE_PASSWORD)

        project.repositories {
            mavenLocal()
            maven { url "${project.ext[PROPERTY_DOWNLOAD_ARTIFACTORY_URL]}/${project.ext[PROPERTY_ARTIFACTORY_RELEASE_REPO]}" }
            mavenCentral()
            maven { url 'https://plugins.gradle.org/m2/' }
        }

        project.plugins.apply(Licenser.class)

        project.tasks.withType(JavaCompile) {
            options.encoding = 'UTF-8'
            if (project.hasProperty('jvmArgs')) {
                options.compilerArgs.addAll(project.jvmArgs.split(','))
            }
        }
        project.tasks.withType(GroovyCompile) {
            options.encoding = 'UTF-8'
            if (project.hasProperty('jvmArgs')) {
                options.compilerArgs.addAll(project.jvmArgs.split(','))
            }
        }
        if (!project.group) {
            project.group = 'com.synopsys.integration'
        }

        configureForJava()
        configureForHeader()
        configureForTesting()
        configureForJacoco()

        if (project.rootProject == project && project.name != 'buildSrc') {
            project.plugins.apply('maven-publish')
            project.plugins.apply(ArtifactoryPlugin.class)

            configureForProjectSetup()
            configureForReleases()
            configureForSonarQube()
        }
    }

    void configureForJava() {
        JavaPluginConvention javaPluginConvention = project.convention.getPlugin(JavaPluginConvention.class)

        javaPluginConvention.sourceCompatibility = project.ext[PROPERTY_JAVA_SOURCE_COMPATIBILITY]
        javaPluginConvention.targetCompatibility = project.ext[PROPERTY_JAVA_TARGET_COMPATIBILITY]

        Task sourcesJarTask = project.tasks.findByName('sourcesJar')
        if (sourcesJarTask == null) {
            project.tasks.create(name: 'sourcesJar', type: Jar) {
                from javaPluginConvention.sourceSets.main.allSource
                //noinspection GroovyAccessibility, GroovyAssignabilityCheck
                archiveClassifier = 'sources'
            }
        }

        Task javadocJarTask = project.tasks.findByName('javadocJar')
        if (javadocJarTask == null) {
            project.tasks.create(name: 'javadocJar', type: Jar) {
                from project.javadoc
                //noinspection GroovyAccessibility, GroovyAssignabilityCheck
                archiveClassifier = 'javadoc'
            }
        }

        if (JavaVersion.current().isJava8Compatible()) {
            project.tasks.withType(Javadoc) {
                options.addStringOption('Xdoclint:none', '-quiet')
            }
        }
    }

    void configureForHeader() {
        LicenseExtension licenseExtension = project.extensions.getByName('license') as LicenseExtension
        licenseExtension.header = project.rootProject.file(HEADER_NAME)

        licenseExtension.properties {
            projectName = project.rootProject.name
            year = Calendar.getInstance().get(Calendar.YEAR)
        }

        //noinspection GroovyAccessibility, GroovyAssignabilityCheck
        licenseExtension.newLine = false
        //noinspection GroovyAccessibility, GroovyAssignabilityCheck
        licenseExtension.ignoreFailures = true

        licenseExtension.include '**/*.groovy'
        licenseExtension.include '**/*.js'
        licenseExtension.include '**/*.java'

        if (project.rootProject == project || project.name == 'buildSrc') {
            registerFileInsertionTask('createHeader', HEADER_NAME, PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_HEADER, HEADER_LOCATION)
        }

        project.tasks.getByName('checkLicenses').dependsOn(project.rootProject.tasks.getByName('createHeader'))
        project.tasks.getByName('checkLicenses').mustRunAfter(project.rootProject.tasks.getByName('createHeader'))
        project.tasks.getByName('updateLicenses').dependsOn(project.rootProject.tasks.getByName('createHeader'))
        project.tasks.getByName('updateLicenses').mustRunAfter(project.rootProject.tasks.getByName('createHeader'))
        project.tasks.getByName('build').dependsOn(project.tasks.getByName('updateLicenses'))
    }

    void configureForProjectSetup() {
        registerFileInsertionTask('createEula', 'EULA.txt', PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_EULA, EULA_LOCATION)
        registerFileInsertionTask('createProjectLicense', 'LICENSE', PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_LICENSE, LICENSE_LOCATION)
        registerFileInsertionTask('createGitIgnore', '.gitignore', PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_GIT_IGNORE, GIT_IGNORE_LOCATION)
        registerFileInsertionTask('createReadme', 'README.md', PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_README, README_LOCATION)
    }

    void configureForReleases() {
        VersionUtility versionUtility = new VersionUtility()
        BuildFileUtility buildFileUtility = new BuildFileUtility()
        File buildFile = project.getBuildFile()

        String buildscriptDependencyLocation = project.ext[PROPERTY_BUILDSCRIPT_DEPENDENCY]

        project.tasks.create('jaloja') {
            doLast {
                try {
                    URL url = new URL(buildscriptDependencyLocation)
                    String remoteContent = url.getText(StandardCharsets.UTF_8.name())

                    String currentContent = "apply from: '${buildscriptDependencyLocation}', to: buildscript"
                    println "Updating ${currentContent} to ${remoteContent}"
                    buildFileUtility.updateBuildScriptDependenciesToRemoteContent(buildFile, remoteContent)

                    String currentVersion = project.version
                    println "Updating current version ${currentVersion} to a release version"
                    String newVersion = versionUtility.calculateReleaseVersion(currentVersion)
                    println "New release version ${newVersion}"
                    project.version = newVersion
                    buildFileUtility.updateVersion(buildFile, currentVersion, newVersion)
                    println "Ja'loja!!!!!"
                } catch (Exception e) {
                    println "Could not get the content for the build script dependencies. ${e.getMessage()}"
                    throw new TaskExecutionException(it as Task, e)
                }
            }
        }

        project.tasks.create('qaJaloja') {
            doLast {
                String currentVersion = project.version
                println "Updating current version ${currentVersion} to a qa version"
                String newVersion = versionUtility.calculateNextQAVersion(currentVersion)
                println "New qa version ${newVersion}"
                project.version = newVersion
                buildFileUtility.updateVersion(buildFile, currentVersion, newVersion)
                println "Ja'loja!!!!!"
            }
        }

        project.tasks.create('snapshotJaloja') {
            doLast {
                String newContent = "apply from: '${buildscriptDependencyLocation}', to: buildscript"
                println "Updating build script dependencies to ${newContent}"
                buildFileUtility.updateBuildScriptDependenciesToApplyFromRemote(buildFile, newContent)

                String currentVersion = project.version
                println "Updating current version ${currentVersion} to a snapshot version"
                String newVersion = versionUtility.calculateNextSnapshot(currentVersion)
                println "New snapshot version ${newVersion}"
                project.version = newVersion
                buildFileUtility.updateVersion(buildFile, currentVersion, newVersion)
                println "Ja'loja!!!!!"
            }
        }
    }

    void configureForTesting() {
        project.dependencies {
            testImplementation 'org.junit.jupiter:junit-jupiter-api:5.7.1'
            testImplementation 'org.junit-pioneer:junit-pioneer:0.3.3'
            testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.7.1'
        }

        def includedTestTags = [] as Set
        includedTestTags.addAll(commaStringToSet(project.ext[PROPERTY_TEST_TAGS_TO_INCLUDE] as String))

        def excludedTestTags = [] as Set
        if (ALL_TEST_TAG != includedTestTags) {
            excludedTestTags.addAll(commaStringToSet(project.ext[PROPERTY_JUNIT_PLATFORM_DEFAULT_TEST_TAGS] as String))
            excludedTestTags.addAll(commaStringToSet(project.ext[PROPERTY_JUNIT_PLATFORM_CUSTOM_TEST_TAGS] as String))
        }

        Closure logging = {
            showStandardStreams = Boolean.valueOf(project.ext[PROPERTY_JUNIT_SHOW_STANDARD_STREAMS] as String)
            project.test.afterSuite { testDescriptor, result ->
                if (!testDescriptor.parent) {
                    println "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)"
                }
            }
        }

        project.test {
            useJUnitPlatform {
                if (!excludedTestTags.isEmpty()) {
                    excludeTags excludedTestTags.toArray(new String[excludedTestTags.size()])
                }
            }
            description += " NOTE: By default, all tagged tests are excluded. To include tag(s), use the project property ${PROPERTY_TEST_TAGS_TO_INCLUDE}. To run all tests, use 'ALL' for the value of ${PROPERTY_TEST_TAGS_TO_INCLUDE}."
            testLogging logging
        }

        if (ALL_TEST_TAG != includedTestTags) {
            includedTestTags.each { includedTestTag ->
                String testTag = includedTestTag.capitalize()
                def options = ['name': 'test' + testTag, 'type': Test.class, 'group': 'Verification']
                Task tagTask = project.tasks.create(options, {
                    useJUnitPlatform {
                        includeTags includedTestTag
                    }
                    description = "Runs all the tests with @Tag(\"${includedTestTag}\")."
                    testLogging logging
                })
                project.test.dependsOn(tagTask)
            }
        }
    }

    void configureForJacoco() {
        project.plugins.apply('jacoco')

        def options = ['name': 'codeCoverageReport', 'type': JacocoReport.class, 'dependsOn': project.test, 'group': 'Verification', 'description': 'Generate code coverage report for tests executed in project.']
        project.tasks.create(options, {
            executionData project.fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec")

            sourceSets project.sourceSets.main

            reports {
                xml.required = true
                xml.destination project.file("${project.buildDir}/reports/jacoco/report.xml")
                html.required = true
                html.destination project.file("${project.buildDir}/reports/jacoco/html")
                csv.required = false
            }
        })
    }

    void configureForSonarQube() {
        def rootProject = project.rootProject
        if (project == rootProject) {
            project.plugins.apply(SonarQubePlugin.class)
            project.tasks.getByName('sonarqube').dependsOn(project.tasks.getByName('codeCoverageReport'))

            def sonarExcludes = project.ext[PROPERTY_EXCLUDES_FROM_TEST_COVERAGE]

            SonarQubeExtension sonarQubeExtension = project.extensions.getByName('sonarqube') as SonarQubeExtension
            def reportFiles = project.fileTree(project.rootDir.absolutePath).include("**/reports/jacoco/report.xml")

            sonarQubeExtension.properties {
                property 'sonar.host.url', 'https://sonarcloud.io'
                property 'sonar.organization', 'black-duck-software'

                property 'sonar.coverage.jacoco.xmlReportPaths', reportFiles
            }

            if (sonarExcludes.size() > 0) {
                println "Applying the following exclusions to your sonarqube task:"
                println "\t" + sonarExcludes

                sonarQubeExtension.properties {
                    property 'sonar.exclusions', sonarExcludes
                }
            }
        }
    }

    void configureDefaultsForArtifactory(String artifactoryRepo) {
        ArtifactoryPluginConvention artifactoryPluginConvention = project.convention.plugins.get('artifactory') as ArtifactoryPluginConvention
        artifactoryPluginConvention.contextUrl = project.ext[PROPERTY_DEPLOY_ARTIFACTORY_URL]

        artifactoryPluginConvention.publish {
            repository { repoKey = artifactoryRepo }
            username = project.ext[PROPERTY_ARTIFACTORY_DEPLOYER_USERNAME]
            password = project.ext[PROPERTY_ARTIFACTORY_DEPLOYER_PASSWORD]
        }

        if (project.ext.has(PROPERTY_ARTIFACTORY_ARTIFACT_NAME)) {
            Closure mavenJava = {
                mavenJava(MavenPublication) {
                    artifact(project.ext[PROPERTY_ARTIFACTORY_ARTIFACT_NAME])
                }
            }

            PublishingExtension publishing = project.extensions.findByName('publishing') as PublishingExtension
            publishing.publications mavenJava
        }
        artifactoryPluginConvention.publisherConfig.defaults({ publications('mavenJava') })

        project.tasks.getByName('artifactoryPublish').dependsOn {
            println "artifactoryPublish will attempt uploading ${project.name}:${project.version} to ${project.ext[PROPERTY_DEPLOY_ARTIFACTORY_URL]}/${project.ext[PROPERTY_ARTIFACTORY_SNAPSHOT_REPO]}"
        }
    }

    private static Set commaStringToSet(String input) {
        return input ? input.split(',').collect { it.trim() } as Set : [] as Set
    }

    private void setExtPropertyOnProject(String propertyName, String propertyDefaults) {
        project.ext[propertyName] = project.findProperty(propertyName)
        if (!project.ext[propertyName]) {
            project.ext[propertyName] = propertyDefaults
        }
    }

    private void setExtPropertyOnProjectNoDefaults(String propertyName, String envVarName) {
        project.ext[propertyName] = project.findProperty(propertyName)
        if (!project.ext[propertyName]) {
            project.ext[propertyName] = System.getenv(envVarName)
        }
    }

    private void registerFileInsertionTask(String taskName, String fileName, String installFlag, String downloadUrl) {
        Task createdTask = project.task(taskName) {
            doLast {
                def projectFile = new File(project.projectDir, fileName)
                if (Boolean.valueOf(project.ext[installFlag] as String)) {
                    if (!projectFile.exists()) {
                        println("Your project did not contain the file ${fileName} but must contain one. The ${fileName} file will be downloaded automatically.")
                        installFile(downloadUrl, projectFile)
                    } else {
                        println "Your project is configured to NOT get the latest ${fileName} - you should be providing your own up-to-date ${fileName} file. No file will be downloaded or updated automatically."
                    }
                } else {
                    println "Your project is configured to get the latest ${fileName} from ${downloadUrl}. The ${fileName} file will be downloaded/updated automatically."
                    installFile(downloadUrl, projectFile)
                }
            }
        }
        project.tasks.getByName('build').dependsOn(createdTask)
    }

    // This can't be private as there is a problem when calling private methods from within a closure.
    // https://stackoverflow.com/questions/54636744/gradle-custom-task-implementation-could-not-find-method-for-arguments
    static void installFile(String downloadUrl, File projectFile) {
        def downloadedFile = new URL(downloadUrl)

        projectFile.withOutputStream { out -> downloadedFile.withInputStream { from -> out << from } }
    }

    void configureAdvancedUsage(String taskName) {
        configureDefaultsForArtifactory(project.ext[PROPERTY_ARTIFACTORY_SNAPSHOT_REPO] as String)

        project.tasks.create(taskName, {
            dependsOn 'artifactoryPublish'
            project.tasks.findByName('artifactoryPublish').mustRunAfter 'build'
        })
    }

    private void displayApplyMessage() {
        if (project.rootProject == project) {
            try {
                URLClassLoader cl = (URLClassLoader) getClass().getClassLoader()
                URL url = cl.findResource("META-INF/MANIFEST.MF")
                Manifest manifest = new Manifest((url.openStream()))
                def applyMessage = manifest.mainAttributes.getValue('Plugin-Apply-Message')

                if (applyMessage?.trim()) {
                    if (applyMessage.contains('SNAPSHOT') && manifest.mainAttributes.getValue('Plugin-Create-Date')) {
                        applyMessage += " created at " + manifest.mainAttributes.getValue('Plugin-Create-Date')
                    }
                    project.logger.lifecycle(applyMessage)
                }
            } catch (IOException ignored) {
                project.logger.warn("Cannot read plugin's manifest. Cosmetic issue.")
            }
        }
    }

}
