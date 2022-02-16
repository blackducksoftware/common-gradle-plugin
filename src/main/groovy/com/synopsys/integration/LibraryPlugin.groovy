package com.synopsys.integration

import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

/*
 * This plugin is intended for common libraries. They will be published to
 * artifactory, using the version (SNAPSHOT or release) to determine the
 * appropriate destination for each.
 */

class LibraryPlugin extends SimplePlugin {
    private Project project

    void apply(Project project) {
        this.project = project
        super.apply(project)
        configureAdvancedUsage('deployLibrary')
        configureForArtifactory(project)
        configureForSigning(project)
    }

    private void configureForArtifactory(Project project) {
        Closure mavenJava = {
            mavenJava(MavenPublication) {
                artifactId = project.rootProject.name
                from project.components.java
                artifact project.sourcesJar
                artifact project.javadocJar
                pom {
                    name = project.rootProject.name
                    description = project.rootProject.description
                    licenses {
                        license {
                            name = 'Apache License 2.0'
                            url = 'http://www.apache.org/licenses/LICENSE-2.0'
                        }
                    }
                }
            }
        }

        PublishingExtension publishing = project.getExtensions().findByName('publishing') as PublishingExtension
        publishing.publications mavenJava
    }

    private void configureForSigning(Project project) {
        if (project.tasks.findByName('publish')) {
            project.plugins.apply('signing')
            SigningExtension signingExtension = project.getExtensions().getByName('signing') as SigningExtension
            signingExtension.setRequired(true)

            PublishingExtension publishing = project.getExtensions().findByName('publishing') as PublishingExtension
            Publication mavenJavaPublication = publishing.getPublications().findByName('mavenJava') as Publication
            signingExtension.sign(mavenJavaPublication)
        }
    }
}
