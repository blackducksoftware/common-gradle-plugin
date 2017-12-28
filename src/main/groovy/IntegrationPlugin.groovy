import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.plugins.signing.SigningExtension
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention

import com.hierynomus.gradle.license.LicenseBasePlugin

import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.NexusStagingPlugin
import nl.javadude.gradle.plugins.license.LicenseExtension

class IntegrationPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.repositories {
            jcenter()
            mavenCentral()
            maven { url 'https://plugins.gradle.org/m2/' }
        }

        project.plugins.apply('java')
        project.plugins.apply('eclipse')
        project.plugins.apply('maven')
        project.plugins.apply('signing')
        project.plugins.apply('jacoco')
        project.plugins.apply(NexusStagingPlugin.class)
        project.plugins.apply(ArtifactoryPlugin.class)
        project.plugins.apply(LicenseBasePlugin.class)

        project.tasks.withType(JavaCompile) { options.encoding = 'UTF-8' }
        project.tasks.withType(GroovyCompile) { options.encoding = 'UTF-8' }

        project.group = 'com.blackducksoftware.integration'

        configureForMavenCentralUpload(project)
        configureForNexusStagingAutoRelease(project)
        configureForArtifactoryUpload(project.rootProject)
        configureLicense(project)

        project.dependencies { testCompile 'junit:junit:4.12' }
    }

    private void configureForMavenCentralUpload(Project project) {
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

        if (JavaVersion.current().isJava8Compatible()) {
            project.tasks.withType(Javadoc) {
                options.addStringOption('Xdoclint:none', '-quiet')
            }
        }

        project.artifacts.add('archives', jarTask)
        project.artifacts.add('archives', sourcesJarTask)
        project.artifacts.add('archives', javadocJarTask)

        NexusStagingExtension nexusStagingExtension = project.extensions.getByName('nexusStaging')
        nexusStagingExtension.packageGroup = 'com.blackducksoftware'

        SigningExtension signingExtension = project.extensions.getByName('signing')
        signingExtension.required {
            project.gradle.taskGraph.hasTask('uploadArchives')
        }
        signingExtension.sign(archivesConfiguration)

        String sonatypeUsername = project.findProperty('sonatypeUsername')
        String sonatypePassword = project.findProperty('sonatypePassword')
        String rootProjectName = project.getRootProject().getName()
        project.uploadArchives {
            repositories {
                mavenDeployer {
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

    private void configureForNexusStagingAutoRelease(Project project) {
        project.getTasks().getByName('closeRepository').onlyIf {
            !project.version.endsWith('-SNAPSHOT')
        }
        project.getTasks().getByName('releaseRepository').onlyIf {
            !project.version.endsWith('-SNAPSHOT')
        }
    }

    private void configureForArtifactoryUpload(Project project) {
        ArtifactoryPluginConvention artifactoryPluginConvention = project.convention.plugins.get('artifactory')
        artifactoryPluginConvention.contextUrl = project.findProperty('artifactoryUrl')
        artifactoryPluginConvention.publish {
            repository {
                repoKey = project.findProperty('artifactoryRepo')
                username = project.findProperty('artifactoryDeployerUsername')
                password = project.findProperty('artifactoryDeployerPassword')
            }
            defaults { publishConfigs ('archives') }
        }
    }

    private void configureLicense(Project project) {
        LicenseExtension licenseExtension = project.extensions.getByName('license')
        licenseExtension.headerURI = new URI('https://blackducksoftware.github.io/common-gradle-plugin/HEADER.txt')
        licenseExtension.ext.year = Calendar.getInstance().get(Calendar.YEAR)
        licenseExtension.ignoreFailures = true
        licenseExtension.includes (["**/*.groovy", "**/*.java"])
        licenseExtension.excludes ([
            "/src/test/*.groovy",
            "src/test/*.java"
        ])

        //task to apply the header to all included files
        Task licenseFormatMainTask = project.tasks.getByName('licenseFormatMain')
        project.tasks.getByName('build').dependsOn(licenseFormatMainTask)
    }
}
