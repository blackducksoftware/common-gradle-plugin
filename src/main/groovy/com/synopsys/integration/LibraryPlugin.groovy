package com.synopsys.integration

import org.gradle.api.Project

/*
 * This plugin is intended for common libraries. They will be published to
 * artifactory, using the version (SNAPSHOT or release) to determine the
 * appropriate destination for each.
 */

class LibraryPlugin extends SimplePlugin {
    void apply(Project project) {
        super.apply(project)

        configureForArtifactoryUpload(project)

        // This must come after the configureForMavenCentralUpload because publishToSonatype does not exist until that is configured
        project.tasks.create('deployLibrary', {
            dependsOn 'artifactoryPublish'
            project.tasks.findByName('artifactoryPublish').mustRunAfter 'build'
        })
    }

    private void configureForArtifactoryUpload(Project project) {
        String artifactoryRepo = project.ext[PROPERTY_ARTIFACTORY_SNAPSHOT_REPO]
        if (!project.isSnapshot) {
            artifactoryRepo = project.ext[PROPERTY_ARTIFACTORY_RELEASE_REPO]
        }

        configureDefaultsForArtifactory(project, artifactoryRepo)
    }

}
