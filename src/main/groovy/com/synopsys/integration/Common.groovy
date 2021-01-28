/*
 * common-gradle-plugin
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration

import com.hierynomus.gradle.license.LicenseBasePlugin
import com.synopsys.integration.utility.BuildFileUtility
import com.synopsys.integration.utility.VersionUtility
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.apache.commons.lang.StringUtils
import org.gradle.api.*
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.kt3k.gradle.plugin.CoverallsPlugin
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin

import java.nio.charset.StandardCharsets

public abstract class Common implements Plugin<Project> {
    public static final String EULA_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/EULA.txt'
    public static final String HEADER_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/HEADER.txt'
    public static final String LICENSE_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/LICENSE'
    public static final String GIT_IGNORE_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/.gitignore'
    public static final String README_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project_init_files/project_default_files/README.md'
    public static final String BUILDSCRIPT_DEPENDENCY_LOCATION = 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle'

    public static final String PROPERTY_DEPLOY_ARTIFACTORY_URL = 'deployArtifactoryUrl'
    public static final String PROPERTY_DOWNLOAD_ARTIFACTORY_URL = 'downloadArtifactoryUrl'
    public static final String PROPERTY_ARTIFACTORY_REPO = 'artifactoryRepo'
    public static final String PROPERTY_ARTIFACTORY_SNAPSHOT_REPO = 'artifactorySnapshotRepo'
    public static final String PROPERTY_ARTIFACTORY_RELEASE_REPO = 'artifactoryReleaseRepo'
    public static final String PROPERTY_JUNIT_PLATFORM_DEFAULT_TEST_TAGS = 'junitPlatformDefaultTestTags'
    public static final String PROPERTY_JUNIT_PLATFORM_CUSTOM_TEST_TAGS = 'junitPlatformCustomTestTags'
    public static final String PROPERTY_JUNIT_SHOW_STANDARD_STREAMS = 'junitShowStandardStreams'
    public static final String PROPERTY_JAVA_SOURCE_COMPATIBILITY = 'javaSourceCompatibility'
    public static final String PROPERTY_JAVA_TARGET_COMPATIBILITY = 'javaTargetCompatibility'
    public static final String PROPERTY_JAVA_USE_AUTO_MODULE_NAME = 'javaUseAutoModuleName'
    public static final String PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_EULA = 'synopsysOverrideIntegrationEula'
    public static final String PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_LICENSE = 'synopsysOverrideIntegrationLicense'
    public static final String PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_GIT_IGNORE = 'synopsysOverrideIntegrationGitIgnore'
    public static final String PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_README = 'synopsysOverrideIntegrationReadme'
    public static final String PROPERTY_BUILDSCRIPT_DEPENDENCY = 'buildscriptDependency'
    public static final String PROPERTY_EXCLUDES_FROM_TEST_COVERAGE = 'excludesFromTestCoverage'

    public static final String PROPERTY_ARTIFACTORY_DEPLOYER_USERNAME = 'artifactoryDeployerUsername'
    public static final String PROPERTY_ARTIFACTORY_DEPLOYER_PASSWORD = 'artifactoryDeployerPassword'
    public static final String ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_USERNAME = 'ARTIFACTORY_DEPLOYER_USER'
    public static final String ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_PASSWORD = 'ARTIFACTORY_DEPLOYER_PASSWORD'

    public static final String PROPERTY_SONATYPE_USERNAME = 'sonatypeUsername'
    public static final String PROPERTY_SONATYPE_PASSWORD = 'sonatypePassword'
    public static final String ENVIRONMENT_VARIABLE_SONATYPE_USERNAME = 'SONATYPE_USERNAME'
    public static final String ENVIRONMENT_VARIABLE_SONATYPE_PASSWORD = 'SONATYPE_PASSWORD'

    void apply(Project project) {
        if (StringUtils.isBlank(project.version) || project.version == 'unspecified') {
            throw new GradleException('The version must be specified before applying this plugin.')
        }

        project.ext.isSnapshot = project.version.endsWith('-SNAPSHOT')
        project.ext[PROPERTY_BUILDSCRIPT_DEPENDENCY] = BUILDSCRIPT_DEPENDENCY_LOCATION

        // assume some reasonable defaults if the environment doesn't provide specific values
        setExtPropertyOnProject(project, PROPERTY_DOWNLOAD_ARTIFACTORY_URL, 'https://sig-repo.synopsys.com')
        setExtPropertyOnProject(project, PROPERTY_ARTIFACTORY_REPO, 'bds-integrations-snapshot')
        setExtPropertyOnProject(project, PROPERTY_ARTIFACTORY_SNAPSHOT_REPO, 'bds-integrations-snapshot')
        setExtPropertyOnProject(project, PROPERTY_ARTIFACTORY_RELEASE_REPO, 'bds-integrations-release')
        setExtPropertyOnProject(project, PROPERTY_JUNIT_PLATFORM_DEFAULT_TEST_TAGS, 'integration, performance')
        setExtPropertyOnProject(project, PROPERTY_JUNIT_PLATFORM_CUSTOM_TEST_TAGS, '')
        setExtPropertyOnProject(project, PROPERTY_JUNIT_SHOW_STANDARD_STREAMS, 'false')
        setExtPropertyOnProject(project, PROPERTY_JAVA_SOURCE_COMPATIBILITY, '1.8')
        setExtPropertyOnProject(project, PROPERTY_JAVA_TARGET_COMPATIBILITY, '1.8')
        setExtPropertyOnProject(project, PROPERTY_JAVA_USE_AUTO_MODULE_NAME, 'false')
        setExtPropertyOnProject(project, PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_EULA, 'false')
        setExtPropertyOnProject(project, PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_LICENSE, 'false')
        setExtPropertyOnProject(project, PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_GIT_IGNORE, 'true')
        setExtPropertyOnProject(project, PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_README, 'true')

        // By default we should not exclude anything
        setExtPropertyOnProject(project, PROPERTY_EXCLUDES_FROM_TEST_COVERAGE, '')

        // there is no default public artifactory for deploying
        setExtPropertyOnProject(project, PROPERTY_DEPLOY_ARTIFACTORY_URL, '')

        // can't assume anything here because passwords have no reasonable defaults
        setExtPropertyOnProjectNoDefaults(project, PROPERTY_ARTIFACTORY_DEPLOYER_USERNAME, ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_USERNAME)
        setExtPropertyOnProjectNoDefaults(project, PROPERTY_ARTIFACTORY_DEPLOYER_PASSWORD, ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_PASSWORD)
        setExtPropertyOnProjectNoDefaults(project, PROPERTY_SONATYPE_USERNAME, ENVIRONMENT_VARIABLE_SONATYPE_USERNAME)
        setExtPropertyOnProjectNoDefaults(project, PROPERTY_SONATYPE_PASSWORD, ENVIRONMENT_VARIABLE_SONATYPE_PASSWORD)

        project.repositories {
            mavenLocal()
            maven { url "${project.ext[PROPERTY_DOWNLOAD_ARTIFACTORY_URL]}/${project.ext[PROPERTY_ARTIFACTORY_RELEASE_REPO]}" }
            mavenCentral()
            maven { url 'https://plugins.gradle.org/m2/' }
        }

        project.plugins.apply('eclipse')
        project.plugins.apply('maven-publish')
        project.plugins.apply(LicenseBasePlugin.class)
        project.plugins.apply(CoverallsPlugin.class)
        project.plugins.apply(ArtifactoryPlugin.class)
        project.plugins.apply(SonarQubePlugin.class)

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

        configureForJava(project)
        configureForLicense(project)
        configureForReleases(project)
        configureForTesting(project)
        configureForJacoco(project)
        configureForSonarQube(project)
    }

    public void configureForJava(Project project) {
        Task jarTask = project.tasks.getByName('jar')
        Task classesTask = project.tasks.getByName('classes')
        Task javadocTask = project.tasks.getByName('javadoc')
        JavaPluginConvention javaPluginConvention = project.convention.getPlugin(JavaPluginConvention.class)

        javaPluginConvention.sourceCompatibility = project.ext[PROPERTY_JAVA_SOURCE_COMPATIBILITY]
        javaPluginConvention.targetCompatibility = project.ext[PROPERTY_JAVA_TARGET_COMPATIBILITY]

        Task sourcesJarTask = project.tasks.findByName('sourcesJar')
        if (sourcesJarTask == null) {
            sourcesJarTask = project.tasks.create(name: 'sourcesJar', type: Jar) {
                from javaPluginConvention.sourceSets.main.allSource
                archiveClassifier = 'sources'
            }
        }

        Task javadocJarTask = project.tasks.findByName('javadocJar')
        if (javadocJarTask == null) {
            javadocJarTask = project.tasks.create(name: 'javadocJar', type: Jar) {
                from project.javadoc
                archiveClassifier = 'javadoc'
            }
        }

        if (JavaVersion.current().isJava8Compatible()) {
            project.tasks.withType(Javadoc) {
                options.addStringOption('Xdoclint:none', '-quiet')
            }
        }
    }

    public void configureForLicense(Project project) {
        LicenseExtension licenseExtension = project.extensions.getByName('license')
        licenseExtension.headerURI = new URI(HEADER_LOCATION)
        licenseExtension.ext.year = Calendar.getInstance().get(Calendar.YEAR)
        licenseExtension.ext.projectName = project.name
        licenseExtension.ignoreFailures = true
        licenseExtension.strictCheck = true
        licenseExtension.includes(['**/*.groovy', '**/*.java', '**/*.js', '**/*.kt'])
        licenseExtension.excludes(['/src/test/*.groovy',
                                   'src/test/*.java',
                                   '**/module-info.java'])
        licenseExtension.mapping('java', 'SLASHSTAR_STYLE')

        //task to apply the header to all included files
        Task licenseFormatMainTask = project.tasks.getByName('licenseFormatMain')
        project.tasks.getByName('build').dependsOn(licenseFormatMainTask)

        registerFileInsertionTask(project, 'createEula', 'EULA.txt', Common.PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_EULA, EULA_LOCATION)
        registerFileInsertionTask(project, 'createProjectLicense', 'LICENSE', Common.PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_LICENSE, LICENSE_LOCATION)
        registerFileInsertionTask(project, 'createGitIgnore', '.gitignore', Common.PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_GIT_IGNORE, GIT_IGNORE_LOCATION)
        registerFileInsertionTask(project, 'createReadme', 'README.md', Common.PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_README, README_LOCATION)
    }

    public void configureForReleases(Project project) {
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
                    throw new TaskExecutionException(it, e)
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

    public void configureForJacoco(Project project) {
        project.plugins.apply('jacoco')

        Task jacocoReportTask = project.tasks.getByName('jacocoTestReport')

        jacocoReportTask.reports {
            // coveralls plugin demands xml format
            xml.enabled = true
            html.enabled = true
        }

        File jacocoDirectory = new File("${project.buildDir}/jacoco")
        if (jacocoDirectory && jacocoDirectory.exists()) {
            jacocoReportTask.executionData(project.files(jacocoDirectory.listFiles()))
        }
    }

    public void configureForSonarQube(Project project) {
        def sonarExcludes = project.ext[PROPERTY_EXCLUDES_FROM_TEST_COVERAGE]

        def surefireReportPaths = ''

        File testResultsDirectory = new File("${project.buildDir}/test-results")
        if (testResultsDirectory && testResultsDirectory.exists()) {
            def allSurefireReportDirectories = project.files(testResultsDirectory.listFiles())
            surefireReportPaths = allSurefireReportDirectories
                    .getFrom()
                    .collect { project.relativePath(it) }
                    .join(',')
        }

        SonarQubeExtension sonarQubeExtension = project.extensions.getByName('sonarqube') as SonarQubeExtension

        sonarQubeExtension.properties {
            property 'sonar.host.url', 'https://sonarcloud.io'
            property 'sonar.organization', 'black-duck-software'

            if (surefireReportPaths) {
                property 'sonar.junit.reportPaths', surefireReportPaths
            }
        }

        if (sonarExcludes.size() > 0) {
            println "Applying the following exclusions to your sonarqube task:"
            println "\t" + sonarExcludes

            sonarQubeExtension.properties {
                property 'sonar.exclusions', sonarExcludes
            }
        }
    }

    public void configureForTesting(Project project) {
        project.dependencies {
            testCompileOnly 'junit:junit:4.12'
            testImplementation 'org.junit.jupiter:junit-jupiter-api:5.3.1'
            testImplementation 'org.junit-pioneer:junit-pioneer:0.3.0'
            testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.3.1'
            testRuntimeOnly 'org.junit.vintage:junit-vintage-engine:5.3.1'
        }

        def allTestTags = ''
        if (project.ext[PROPERTY_JUNIT_PLATFORM_DEFAULT_TEST_TAGS]) {
            allTestTags += project.ext[PROPERTY_JUNIT_PLATFORM_DEFAULT_TEST_TAGS]
        }
        if (project.ext[PROPERTY_JUNIT_PLATFORM_CUSTOM_TEST_TAGS]) {
            allTestTags += ',' + project.ext[PROPERTY_JUNIT_PLATFORM_CUSTOM_TEST_TAGS]
        }
        def testTags = allTestTags.split("\\s*,\\s*")

        def descriptionSuffix = testTags.collect { "@Tag(\"${it}\")" }.join(" or ")
        project.test {
            useJUnitPlatform {
                excludeTags testTags
            }
            description += " NOTE: This excludes those tests with ${descriptionSuffix})."
            testLogging.showStandardStreams = Boolean.valueOf(project.ext[PROPERTY_JUNIT_SHOW_STANDARD_STREAMS])
        }

        testTags.each { testTag ->
            project.tasks.create('test' + testTag.capitalize(), Test) {
                useJUnitPlatform { includeTags testTag }
                group = 'verification'
                description = "Runs all the tests with @Tag(\"${testTag}\")."
                testLogging.showStandardStreams = Boolean.valueOf(project.ext[PROPERTY_JUNIT_SHOW_STANDARD_STREAMS])
            }
        }

        project.tasks.create('testAll', Test) {
            useJUnitPlatform()
            group = 'verification'
            description = "Runs all the tests (ignores tags)."
            testLogging.showStandardStreams = Boolean.valueOf(project.ext[PROPERTY_JUNIT_SHOW_STANDARD_STREAMS])
        }
    }

    public void configureDefaultsForArtifactory(Project project, String artifactoryRepo) {
        configureDefaultsForArtifactory(project, artifactoryRepo, null)
    }

    public void configureDefaultsForArtifactory(Project project, String artifactoryRepo, Closure defaultsClosure) {
        ArtifactoryPluginConvention artifactoryPluginConvention = project.convention.plugins.get('artifactory')
        artifactoryPluginConvention.contextUrl = project.ext[PROPERTY_DEPLOY_ARTIFACTORY_URL]
        artifactoryPluginConvention.publish {
            repository { repoKey = artifactoryRepo }
            username = project.ext[PROPERTY_ARTIFACTORY_DEPLOYER_USERNAME]
            password = project.ext[PROPERTY_ARTIFACTORY_DEPLOYER_PASSWORD]
        }

        if (defaultsClosure != null) {
            artifactoryPluginConvention.publisherConfig.defaults(defaultsClosure)
        }

        project.tasks.getByName('artifactoryPublish').dependsOn {
            println "artifactoryPublish will attempt uploading ${project.name}:${project.version} to ${project.ext[PROPERTY_DEPLOY_ARTIFACTORY_URL]}/${project.ext[PROPERTY_ARTIFACTORY_REPO]}"
        }
    }

    private void setExtPropertyOnProject(Project project, String propertyName, String propertyDefaults) {
        project.ext[propertyName] = project.findProperty(propertyName)
        if (!project.ext[propertyName]) {
            project.ext[propertyName] = propertyDefaults
        }
    }

    private void setExtPropertyOnProjectNoDefaults(Project project, String propertyName, String envVarName) {
        project.ext[propertyName] = project.findProperty(propertyName)
        if (!project.ext[propertyName]) {
            project.ext[propertyName] = System.getenv(envVarName)
        }
    }

    private void registerFileInsertionTask(Project project, String taskName, String fileName, String installFlag, String downloadUrl) {
        Task createdTask = project.task(taskName) {
            doLast {
                if (project.rootProject == project) {
                    def projectFile = new File(project.projectDir, fileName)
                    if (Boolean.valueOf(project.ext[installFlag])) {
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
        }
        project.tasks.getByName('build').dependsOn(createdTask)
    }

    // This can't be private as there is a problem when calling private methods from within a closure.
    // https://stackoverflow.com/questions/54636744/gradle-custom-task-implementation-could-not-find-method-for-arguments
    void installFile(String downloadUrl, File projectFile) {
        def downloadedFile = new URL(downloadUrl)

        projectFile.withOutputStream { out -> downloadedFile.withInputStream { from -> out << from } }
    }

}
