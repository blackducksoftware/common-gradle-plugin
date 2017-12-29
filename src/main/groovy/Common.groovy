/*
 * common-gradle-plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin

import com.hierynomus.gradle.license.LicenseBasePlugin

import nl.javadude.gradle.plugins.license.LicenseExtension

abstract class Common implements Plugin<Project> {
    void apply(Project project) {
        project.repositories {
            jcenter()
            mavenCentral()
            maven { url 'https://plugins.gradle.org/m2/' }
        }

        project.plugins.apply('java')
        project.plugins.apply('eclipse')
        project.plugins.apply('maven')
        project.plugins.apply('jacoco')
        project.plugins.apply(LicenseBasePlugin.class)
        project.plugins.apply(ArtifactoryPlugin.class)

        project.tasks.withType(JavaCompile) { options.encoding = 'UTF-8' }
        project.tasks.withType(GroovyCompile) { options.encoding = 'UTF-8' }

        project.group = 'com.blackducksoftware.integration'
        project.dependencies { testCompile 'junit:junit:4.12' }

        configureForJava(project)
        configureForLicense(project)
    }

    public void configureForJava(Project project) {
        Task jarTask = project.getTasks().getByName('jar')
        Task classesTask = project.getTasks().getByName('classes')
        Task javadocTask = project.getTasks().getByName('javadoc')
        Configuration archivesConfiguration = project.getConfigurations().getByName('archives')
        JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class)

        javaPluginConvention.sourceCompatibility = 1.7
        javaPluginConvention.targetCompatibility = 1.7

        Task sourcesJarTask = project.tasks.create(name: 'sourcesJar', type: Jar, dependsOn: classesTask) {
            classifier = 'sources'
            from javaPluginConvention.sourceSets.main.allSource
        }

        Task javadocJarTask = project.tasks.create(name: 'javadocJar', type: Jar, dependsOn: javadocTask) {
            classifier = 'javadoc'
            from javadocTask.destinationDir
        }

        if (JavaVersion.current().isJava8Compatible()) {
            project.tasks.withType(Javadoc) {
                options.addStringOption('Xdoclint:none', '-quiet')
            }
        }

        project.artifacts.add('archives', jarTask)
        project.artifacts.add('archives', sourcesJarTask)
        project.artifacts.add('archives', javadocJarTask)
    }

    public void configureForLicense(Project project) {
        LicenseExtension licenseExtension = project.extensions.getByName('license')
        licenseExtension.headerURI = new URI('https://blackducksoftware.github.io/common-gradle-plugin/HEADER.txt')
        licenseExtension.ext.year = Calendar.getInstance().get(Calendar.YEAR)
        licenseExtension.ext.projectName = project.name
        licenseExtension.ignoreFailures = true
        licenseExtension.includes (['**/*.groovy', '**/*.java'])
        licenseExtension.excludes ([
            '/src/test/*.groovy',
            'src/test/*.java'
        ])

        //task to apply the header to all included files
        Task licenseFormatMainTask = project.tasks.getByName('licenseFormatMain')
        project.tasks.getByName('build').dependsOn(licenseFormatMainTask)
    }
}