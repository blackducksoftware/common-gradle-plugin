/*
 * common-gradle-plugin
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import com.hierynomus.gradle.license.LicenseBasePlugin
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.apache.commons.lang.StringUtils
import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginConvention
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

abstract class Common implements Plugin<Project> {
    public static final String EULA_LOCATION = 'https://blackducksoftware.github.io/integration-resources/project/EULA.txt'

    public static final PROPERTY_DEPLOY_ARTIFACTORY_URL = 'deployArtifactoryUrl'
    public static final PROPERTY_DOWNLOAD_ARTIFACTORY_URL = 'downloadArtifactoryUrl'
    public static final PROPERTY_ARTIFACTORY_REPO = 'artifactoryRepo'
    public static final PROPERTY_ARTIFACTORY_SNAPSHOT_REPO = 'artifactorySnapshotRepo'
    public static final PROPERTY_ARTIFACTORY_RELEASE_REPO = 'artifactoryReleaseRepo'
    public static final PROPERTY_JUNIT_PLATFORM_DEFAULT_TEST_TAGS = 'junitPlatformDefaultTestTags'
    public static final PROPERTY_JUNIT_PLATFORM_CUSTOM_TEST_TAGS = 'junitPlatformCustomTestTags'
    public static final PROPERTY_JUNIT_SHOW_STANDARD_STREAMS = 'junitShowStandardStreams'
    public static final PROPERTY_JAVA_SOURCE_COMPATIBILITY = 'javaSourceCompatibility'
    public static final PROPERTY_JAVA_TARGET_COMPATIBILITY = 'javaTargetCompatibility'
    public static final PROPERTY_JAVA_USE_AUTO_MODULE_NAME = 'javaUseAutoModuleName'
    public static final PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_EULA = 'synopsysOverrideIntegrationEula'

    public static final PROPERTY_ARTIFACTORY_DEPLOYER_USERNAME = 'artifactoryDeployerUsername'
    public static final PROPERTY_ARTIFACTORY_DEPLOYER_PASSWORD = 'artifactoryDeployerPassword'
    public static final ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_USERNAME = 'ARTIFACTORY_DEPLOYER_USER'
    public static final ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_PASSWORD = 'ARTIFACTORY_DEPLOYER_PASSWORD'

    public static final PROPERTY_SONATYPE_USERNAME = 'sonatypeUsername'
    public static final PROPERTY_SONATYPE_PASSWORD = 'sonatypePassword'
    public static final ENVIRONMENT_VARIABLE_SONATYPE_USERNAME = 'SONATYPE_USERNAME'
    public static final ENVIRONMENT_VARIABLE_SONATYPE_PASSWORD = 'SONATYPE_PASSWORD'

    public static final PROPERTY_SONAR_QUBE_LOGIN = 'sonarQubeLogin'
    public static final ENVIRONMENT_VARIABLE_SONAR_QUBE_LOGIN = 'SONAR_QUBE_LOGIN'

    void apply(Project project) {
        if (StringUtils.isBlank(project.version) || project.version == 'unspecified') {
            throw new GradleException('The version must be specified before applying this plugin.')
        }

        project.ext.isSnapshot = project.version.endsWith('-SNAPSHOT')

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

        // can't assume anything here because passwords have no reasonable defaults
        setExtPropertyOnProjectNoDefaults(project, PROPERTY_ARTIFACTORY_DEPLOYER_USERNAME, ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_USERNAME)
        setExtPropertyOnProjectNoDefaults(project, PROPERTY_ARTIFACTORY_DEPLOYER_PASSWORD, ENVIRONMENT_VARIABLE_ARTIFACTORY_DEPLOYER_PASSWORD)
        setExtPropertyOnProjectNoDefaults(project, PROPERTY_SONATYPE_USERNAME, ENVIRONMENT_VARIABLE_SONATYPE_USERNAME)
        setExtPropertyOnProjectNoDefaults(project, PROPERTY_SONATYPE_PASSWORD, ENVIRONMENT_VARIABLE_SONATYPE_PASSWORD)
        setExtPropertyOnProjectNoDefaults(project, PROPERTY_SONAR_QUBE_LOGIN, ENVIRONMENT_VARIABLE_SONAR_QUBE_LOGIN)

        project.repositories {
            mavenLocal()
            maven { url "${project.ext.downloadArtifactoryUrl}/${project.ext.artifactoryReleaseRepo}"}
            mavenCentral()
            maven { url 'https://plugins.gradle.org/m2/' }
        }

        project.plugins.apply('eclipse')
        project.plugins.apply('maven-publish')
        project.plugins.apply('jacoco')
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
            project.group = 'com.blackducksoftware.integration'
        }

        configureForJava(project)
        configureForLicense(project)
        configureForSonarQube(project)
        configureForTesting(project)
    }

    public void configureForJava(Project project) {
        Task jarTask = project.tasks.getByName('jar')
        Task classesTask = project.tasks.getByName('classes')
        Task javadocTask = project.tasks.getByName('javadoc')
        JavaPluginConvention javaPluginConvention = project.convention.getPlugin(JavaPluginConvention.class)

        javaPluginConvention.sourceCompatibility = project.ext.javaSourceCompatibility
        javaPluginConvention.targetCompatibility = project.ext.javaTargetCompatibility

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

        project.tasks.getByName('jacocoTestReport').reports {
            // coveralls plugin demands xml format
            xml.enabled = true
            html.enabled = true
        }
    }

    public void configureForLicense(Project project) {
        LicenseExtension licenseExtension = project.extensions.getByName('license')
        licenseExtension.headerURI = new URI('https://blackducksoftware.github.io/integration-resources/project/HEADER.txt')
        licenseExtension.ext.year = Calendar.getInstance().get(Calendar.YEAR)
        licenseExtension.ext.projectName = project.name
        licenseExtension.ignoreFailures = true
        licenseExtension.strictCheck = true
        licenseExtension.includes(['**/*.groovy', '**/*.java', '**/*.js'])
        licenseExtension.excludes(['/src/test/*.groovy',
                                   'src/test/*.java',
                                   '**/module-info.java'])

        //task to apply the header to all included files
        Task licenseFormatMainTask = project.tasks.getByName('licenseFormatMain')
        project.tasks.getByName('build').dependsOn(licenseFormatMainTask)

        Task createEulaTask = project.task('createEula') {
            doLast {
                if (project.rootProject == project) {
                    def eulaFile = new File(project.projectDir, 'EULA.txt')
                    if (Boolean.valueOf(project.ext[Common.PROPERTY_SYNOPSYS_OVERRIDE_INTEGRATION_EULA])) {
                        println 'Your project is configured to NOT get the latest EULA - you should be providing your own up-to-date EULA.txt file. No file will be downloaded or updated automatically.'
                    } else {
                        println "Your project is configured to get the latest EULA from ${EULA_LOCATION}. The EULA.txt file will be downloaded/updated automatically."
                        def eulaUrl = new URL(EULA_LOCATION)

                        eulaFile.withOutputStream { out ->
                            eulaUrl.withInputStream { from -> out << from }
                        }
                    }
                }
            }
        }
        project.tasks.getByName('build').dependsOn(createEulaTask)
    }

    public void configureForSonarQube(Project project) {
        SonarQubeExtension sonarQubeExtension = project.extensions.getByName('sonarqube')
        sonarQubeExtension.properties {
            property 'sonar.host.url', 'https://sonarcloud.io'
            property 'sonar.organization', 'black-duck-software'
            property 'sonar.login', project.ext.sonarQubeLogin
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
        if (project.ext.junitPlatformDefaultTestTags) {
            allTestTags += project.ext.junitPlatformDefaultTestTags
        }
        if (project.ext.junitPlatformCustomTestTags) {
            allTestTags += ',' + project.ext.junitPlatformCustomTestTags
        }
        def testTags = allTestTags.split("\\s*,\\s*")

        def descriptionSuffix = testTags.collect { "@Tag(\"${it}\")" }.join(" or ")
        project.test {
            useJUnitPlatform {
                excludeTags testTags
            }
            description += " NOTE: This excludes those tests with ${descriptionSuffix})."
            testLogging.showStandardStreams = Boolean.valueOf(project.ext.junitShowStandardStreams)
        }

        testTags.each { testTag ->
            project.tasks.create('test' + testTag.capitalize(), Test) {
                useJUnitPlatform { includeTags testTag }
                group = 'verification'
                description = "Runs all the tests with @Tag(\"${testTag}\")."
                testLogging.showStandardStreams = Boolean.valueOf(project.ext.junitShowStandardStreams)
            }
        }

        project.tasks.create('testAll', Test) {
            useJUnitPlatform()
            group = 'verification'
            description = "Runs all the tests (ignores tags)."
            testLogging.showStandardStreams = Boolean.valueOf(project.ext.junitShowStandardStreams)
        }
    }

    public void configureDefaultsForArtifactory(Project project, String artifactoryRepo) {
        configureDefaultsForArtifactory(project, artifactoryRepo, null)
    }

    public void configureDefaultsForArtifactory(Project project, String artifactoryRepo, Closure defaultsClosure) {
        ArtifactoryPluginConvention artifactoryPluginConvention = project.convention.plugins.get('artifactory')
        artifactoryPluginConvention.publish {
            contextUrl = project.ext.deployArtifactoryUrl
            repository { repoKey = artifactoryRepo }
            username = project.ext.artifactoryDeployerUsername
            password = project.ext.artifactoryDeployerPassword
        }

        if (defaultsClosure != null) {
            artifactoryPluginConvention.publisherConfig.defaults(defaultsClosure)
        }

        project.tasks.getByName('artifactoryPublish').dependsOn {
            println "artifactoryPublish will attempt uploading ${project.name}:${project.version} to ${project.ext.deployArtifactoryUrl}/${artifactoryRepo}"
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

}
