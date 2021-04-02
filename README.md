# Common Plugin For Integrations

# How to release

1. Edit build.gradle and remove '-SNAPSHOT' from the version.
2. Run the Jenkins release job.
3. Tag the release commit with the version number.
4. Edit the build.gradle and update the version to the next [semVer](https://semver.org/) SNAPSHOT.
5. Create a new release for the tag in GitHub and upload the artifacts from Jenkins.
6. Go to [integration-resources](https://github.com/blackducksoftware/integration-resources/) and edit
   the [buildscript.gradle](https://github.com/blackducksoftware/integration-resources/blob/master/gradle_common/buildscript-dependencies.gradle) file to use the latest version of common-gradle-plugin.
