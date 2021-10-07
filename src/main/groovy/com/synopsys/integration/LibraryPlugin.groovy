package com.synopsys.integration

import org.gradle.api.Project

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
    }

}
