/*
 * common-gradle-plugin
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration

import org.gradle.api.Project

/**
 * This plugin is meant for final integration solutions. They can create the
 * 'mavenJava' publication for uploading to artifactory, overloading the
 * 'artifactoryRepo' property to affect the destination repository.*/
public class SolutionPlugin extends Common {
    void apply(Project project) {
        project.plugins.apply('java')

        super.apply(project)

        project.tasks.create('deploySolution', {
            dependsOn 'artifactoryPublish'
            project.tasks.findByName('artifactoryPublish').mustRunAfter 'build'
        })

        configureForArtifactoryUpload(project)
    }

    private void configureForArtifactoryUpload(Project project) {
        String artifactoryRepo = project.ext[PROPERTY_ARTIFACTORY_REPO]
        configureDefaultsForArtifactory(project, artifactoryRepo)
    }

}
