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
 * This plugin is intended for simple java/groovy projects that do not need publishing.*/
public class SimplePlugin extends Common {
    void apply(Project project) {
        project.plugins.apply("java-library")

        super.apply(project)

        if (Boolean.valueOf(project.ext[Common.PROPERTY_JAVA_USE_AUTO_MODULE_NAME]) && project.ext.moduleName) {
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
