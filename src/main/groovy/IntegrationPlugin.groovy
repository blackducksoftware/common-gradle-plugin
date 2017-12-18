import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.plugins.signing.SigningExtension

import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.NexusStagingPlugin

class IntegrationPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply('java')
        project.plugins.apply('eclipse')
        project.plugins.apply('maven')
        project.plugins.apply('signing')
        project.plugins.apply(NexusStagingPlugin.class)

        project.tasks.withType(JavaCompile) { options.encoding = 'UTF-8' }
        project.tasks.withType(GroovyCompile) { options.encoding = 'UTF-8' }

        project.group = 'com.blackducksoftware.integration'

        Task jarTask = project.getTasks().getByName('jar')
        Task classesTask = project.getTasks().getByName('classes')
        Task javadocTask = project.getTasks().getByName('javadoc')
        Configuration archivesConfiguration = project.getConfigurations().getByName('archives')
        JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class)

        Task sourcesJarTask = project.tasks.create(name: 'sourcesJar', type: Jar, dependsOn: classesTask) {
            classifier = 'sources'
            from javaPluginConvention.sourceSets.main.allSource
        }

        Task javadocJarTask = project.tasks.create(name: 'javadocJar', type: Jar, dependsOn: javadocTask) {
            classifier = 'javadoc'
            from javadocTask.destinationDir
        }

        project.artifacts.add('archives', jarTask)
        project.artifacts.add('archives', sourcesJarTask)
        project.artifacts.add('archives', javadocJarTask)

        NexusStagingExtension nexusStagingExtension = project.extensions.getByName('nexusStaging')
        nexusStagingExtension.packageGroup = 'com.blackducksoftware'

        SigningExtension signingExtension = project.extensions.getByName('signing')
        signingExtension.required { project.gradle.taskGraph.hasTask("uploadArchives") }
        signingExtension.sign(archivesConfiguration)

        String sonatypeUsername = project.findProperty('sonatypeUsername')
        String sonatypePassword = project.findProperty('sonatypePassword')
        String rootProjectName = project.getRootProject().getName()
        project.uploadArchives {
            repositories {
                mavenDeployer{
                    beforeDeployment { MavenDeployment deployment ->
                        signingExtension.signPom(deployment)
                    }
                    repository(url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/') {
                        authentication(userName: sonatypeUsername, password: sonatypePassword)
                    }
                    snapshotRepository(url: 'https://oss.sonatype.org/content/repositories/snapshots/') {
                        authentication(userName: sonatypeUsername, password: sonatypePassword)
                    }
                    pom.project {
                        name project.rootProject.name
                        description project.rootProject.description
                        url "https://www.github.com/blackducksoftware/${rootProjectName}"
                        packaging 'jar'
                        scm {
                            connection "scm:git:git://github.com/blackducksoftware/${rootProjectName}.git"
                            developerConnection "scm:git:git@github.com:blackducksoftware/${rootProjectName}.git"
                            url "https://www.github.com/blackducksoftware/${rootProjectName}"
                        }
                        licenses {
                            license {
                                name 'Apache License 2.0'
                                url 'http://www.apache.org/licenses/LICENSE-2.0'
                            }
                        }
                        developers {
                            developer {
                                id 'blackduckoss'
                                name 'Black Duck OSS'
                                email 'bdsoss@blackducksoftware.com'
                                organization 'Black Duck Software, Inc.'
                                organizationUrl 'http://www.blackducksoftware.com'
                                roles { role 'developer' }
                                timezone 'America/New_York'
                            }
                        }
                    }
                }
            }
        }
    }
}
