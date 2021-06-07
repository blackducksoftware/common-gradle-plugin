package com.synopsys.integration.utility

class BuildFileUtility {
    public static final String START_BUILDSCRIPT_DEPENDENCY = "////////// START BUILDSCRIPT DEPENDENCY //////////"
    public static final String END_BUILDSCRIPT_DEPENDENCY = "////////// END BUILDSCRIPT DEPENDENCY //////////"
    public static final String APPLY_FROM_REMOTE = "apply from: 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle', to: buildscript"

    void updateVersion(File buildFile, String currentVersion, String newVersion) {
        String buildFileContents = buildFile.text
        String newBuildFileContents = updateVersion(buildFileContents, currentVersion, newVersion)

        buildFile.text = newBuildFileContents
    }

    String updateVersion(String buildFileContents, String currentVersion, String newVersion) {
        String versionLinePattern = getVersionLinePattern(currentVersion)
        String newVersionLine = getNewVersionLine(newVersion)

        String newContents = buildFileContents.replaceAll(versionLinePattern, newVersionLine)
        return newContents
    }

    void updateBuildScriptDependenciesToRemoteContent(File buildFile, String remoteContent) {
        String buildFileContents = buildFile.text
        String newBuildFileContents = updateBuildScriptDependenciesToRemoteContent(buildFileContents, remoteContent)

        buildFile.text = newBuildFileContents
    }

    String updateBuildScriptDependenciesToRemoteContent(String buildFileContents, String remoteContent) {
        String currentContent = getBuildScriptDependencyLinePattern()

        String newReplacement = START_BUILDSCRIPT_DEPENDENCY + '\n' + remoteContent + '\n' + END_BUILDSCRIPT_DEPENDENCY

        String newContents = buildFileContents.replaceAll(currentContent, newReplacement)
        return newContents
    }

    void updateBuildScriptDependenciesToApplyFromRemote(File buildFile, String replacement) {
        String buildFileContents = buildFile.text
        String newBuildFileContents = updateBuildScriptDependenciesToApplyFromRemote(buildFileContents, replacement)

        buildFile.text = newBuildFileContents
    }

    String updateBuildScriptDependenciesToApplyFromRemote(String buildFileContents, String replacement) {
        String newContents = buildFileContents
        int startIndex = buildFileContents.indexOf(START_BUILDSCRIPT_DEPENDENCY)
        int endIndex = buildFileContents.indexOf(END_BUILDSCRIPT_DEPENDENCY)
        if (startIndex > -1 && endIndex > -1) {
            newContents = buildFileContents[0..startIndex - 1] + replacement + buildFileContents[endIndex + END_BUILDSCRIPT_DEPENDENCY.length()..-1]
        }
        return newContents
    }

    String getBuildScriptDependencyLinePattern() {
        //    apply from: 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-dependencies.gradle', to: buildscript
        return "apply\\sfrom:\\s[\"\']?.*buildscript-dependencies.gradle[\"\']?,\\sto:\\sbuildscript"
    }

    String getVersionLinePattern(String currentVersion) {
        return "version\\s*=\\s*[\"\']?${currentVersion}[\"\']?"
    }

    String getNewVersionLine(String version) {
        return "version = '${version}'"
    }

}
