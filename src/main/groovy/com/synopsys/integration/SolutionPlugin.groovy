package com.synopsys.integration

import org.gradle.api.Project

/*
 * This plugin is meant for final integration solutions. They can create the
 * 'mavenJava' publication for uploading to artifactory, overloading the
 * 'artifactoryRepo' property to affect the destination repository.
 *
 * As part of a release, a solution will be made available publicly in
 * Artifactory.
 */

class SolutionPlugin extends Common {
    private Project project

    void apply(Project project) {
        this.project = project
        project.plugins.apply('java')
        super.apply(project)
        configureAdvancedUsage('deploySolution')
    }

}
