/*
 * common-gradle-plugin
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */

package com.synopsys.integration

/*
 * common-gradle-plugin
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import org.gradle.api.Project

/**
 * This plugin is meant for final integration solutions. They can create the
 * 'mavenJava' publication for uploading to artifactory, overloading the
 * 'artifactoryRepo' property to affect the destination repository.*/
class SolutionPlugin extends Common {
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
