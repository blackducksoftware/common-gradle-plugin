package com.synopsys.integration

import org.gradle.api.Project

/*
 * This plugin is intended for simple java/groovy projects that do not need publishing.
 */

class SimplePlugin extends Common {
    void apply(Project project) {
        project.plugins.apply("java-library")

        super.apply(project)

        if (Boolean.valueOf(project.ext[PROPERTY_JAVA_USE_AUTO_MODULE_NAME]) && project.ext.has('moduleName')) {
            def moduleName = project.ext.moduleName
            project.tasks.getByName('jar') {
                inputs.property("moduleName", moduleName)
                manifest {
                    attributes('Automatic-Module-Name': moduleName)
                }
            }
        }
    }

}
