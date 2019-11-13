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


import de.marcphilipp.gradle.nexus.NexusPublishExtension
import de.marcphilipp.gradle.nexus.NexusPublishPlugin
import de.marcphilipp.gradle.nexus.NexusRepositoryContainer
import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.NexusStagingPlugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

import java.time.Duration

/**
 * This plugin is intended for common libraries. They will be published to
 * maven central and artifactory, using the version (SNAPSHOT or release) to
 * determine the appropriate destination for each.*/
class LibraryPlugin extends SimplePlugin {
    void apply(Project project) {
        super.apply(project)

        project.plugins.apply('signing')
        project.plugins.apply(NexusStagingPlugin.class)
        project.plugins.apply(NexusPublishPlugin.class)

        configureForMavenCentralUpload(project)
        configureForArtifactoryUpload(project)
        configureForNexusStagingAutoRelease(project)

        project.tasks.create('deployLibrary', {
            dependsOn 'artifactoryPublish'
            dependsOn 'publish'
            dependsOn 'publishToSonatype'
            dependsOn 'closeAndReleaseRepository'
            project.tasks.findByName('artifactoryPublish').mustRunAfter 'build'
            project.tasks.findByName('publish').mustRunAfter 'build'
            project.tasks.findByName('publishToSonatype').mustRunAfter 'publish'
            project.tasks.findByName('closeAndReleaseRepository').mustRunAfter 'publishToSonatype'
        })
    }

    private void configureForMavenCentralUpload(Project project) {
        String sonatypeUsername = project.ext.sonatypeUsername
        String sonatypePassword = project.ext.sonatypePassword
        NexusStagingExtension nexusStagingExtension = project.extensions.getByName('nexusStaging')

        if (null == nexusStagingExtension.packageGroup || nexusStagingExtension.packageGroup.trim().equals("")) {
            nexusStagingExtension.packageGroup = 'com.blackducksoftware'
            nexusStagingExtension.stagingProfileId = '324d855ef1398'
        }
        nexusStagingExtension.username = sonatypeUsername
        nexusStagingExtension.password = sonatypePassword

        NexusPublishExtension nexusPublishExtension = project.extensions.getByName('nexusPublishing')
        nexusPublishExtension.repositories = NexusRepositoryContainer.sonatype()
        nexusPublishExtension.clientTimeout = Duration.ofMinutes(5)
        nexusPublishExtension.connectTimeout = Duration.ofMinutes(5)

        project.publishing {
            publications {
                mavenJava(MavenPublication) {
                    artifactId = project.rootProject.name
                    from project.components.java
                    artifact project.sourcesJar
                    artifact project.javadocJar
                    versionMapping {
                        usage('java-api') {
                            fromResolutionOf('runtimeClasspath')
                        }
                        usage('java-runtime') {
                            fromResolutionResult()
                        }
                    }
                    pom {
                        name = project.rootProject.name
                        description = project.rootProject.description
                        url = "https://www.github.com/blackducksoftware/${project.rootProject.name}"
                        licenses {
                            license {
                                name = 'Apache License 2.0'
                                url = 'http://www.apache.org/licenses/LICENSE-2.0'
                            }
                        }
                        developers {
                            developer {
                                id = 'blackduckoss'
                                name = 'Black Duck OSS'
                                email = 'bdsoss@blackducksoftware.com'
                                organization = 'Black Duck Software, Inc.'
                                organizationUrl = 'http://www.blackducksoftware.com'
                                timezone = 'America/New_York'
                            }
                        }
                        scm {
                            connection = "scm:git:git://github.com/blackducksoftware/${project.rootProject.name}.git"
                            developerConnection = "scm:git:git@github.com:blackducksoftware/${project.rootProject.name}.git"
                            url = "https://www.github.com/blackducksoftware/${project.rootProject.name}"
                        }
                    }
                }
            }
        }

        project.tasks.getByName('publish').dependsOn { println "publish will attempt uploading ${project.name}:${project.version} to maven central" }

        SigningExtension signingExtension = project.extensions.getByName('signing')
        signingExtension.required {
            project.gradle.taskGraph.hasTask('publish')
        }
        def mavenJavaPublication = project.publishing.publications.findByName('mavenJava')
        signingExtension.sign(mavenJavaPublication)
    }

    private void configureForArtifactoryUpload(Project project) {
        String artifactoryRepo = project.ext.artifactorySnapshotRepo
        if (!project.isSnapshot) {
            artifactoryRepo = project.ext.artifactoryReleaseRepo
        }

        configureDefaultsForArtifactory(project, artifactoryRepo, { publications ('mavenJava') })
    }

    private void configureForNexusStagingAutoRelease(Project project) {
        project.tasks.getByName('publishToSonatype').onlyIf { !project.isSnapshot }
        project.tasks.getByName('publishToSonatype').dependsOn 'publish'
        project.tasks.getByName('closeRepository').onlyIf { !project.isSnapshot }
        project.tasks.getByName('closeRepository').dependsOn 'publishToSonatype'
        project.tasks.getByName('releaseRepository').onlyIf { !project.isSnapshot }
        project.tasks.getByName('releaseRepository').dependsOn 'publishToSonatype'
    }

}
