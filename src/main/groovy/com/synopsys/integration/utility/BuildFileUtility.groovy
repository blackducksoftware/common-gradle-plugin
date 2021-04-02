/*
 * common-gradle-plugin
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.utility

public class BuildFileUtility {
    public static final String START_BUILDSCRIPT_DEPENDENCY = "////////// START BUILDSCRIPT DEPENDENCY //////////"
    public static final String END_BUILDSCRIPT_DEPENDENCY = "////////// END BUILDSCRIPT DEPENDENCY //////////"

    public void updateVersion(File buildFile, String currentVersion, String newVersion) {
        String buildFileContents = buildFile.text
        String newBuildFileContents = updateVersion(buildFileContents, currentVersion, newVersion)

        buildFile.text = newBuildFileContents
    }

    public String updateVersion(String buildFileContents, String currentVersion, String newVersion) {
        String versionLinePattern = getVersionLinePattern(currentVersion)
        String newVersionLine = getNewVersionLine(newVersion)

        String newContents = buildFileContents.replaceAll(versionLinePattern, newVersionLine)
        return newContents
    }

    public void updateBuildScriptDependenciesToRemoteContent(File buildFile, String remoteContent) {
        String buildFileContents = buildFile.text
        String newBuildFileContents = updateBuildScriptDependenciesToRemoteContent(buildFileContents, remoteContent)

        buildFile.text = newBuildFileContents
    }

    public String updateBuildScriptDependenciesToRemoteContent(String buildFileContents, String remoteContent) {
        String currentContent = getBuildScriptDependencyLinePattern()

        String newReplacement = START_BUILDSCRIPT_DEPENDENCY + '\n' + remoteContent + '\n' + END_BUILDSCRIPT_DEPENDENCY

        String newContents = buildFileContents.replaceAll(currentContent, newReplacement)
        return newContents
    }

    public void updateBuildScriptDependenciesToApplyFromRemote(File buildFile, String replacement) {
        String buildFileContents = buildFile.text
        String newBuildFileContents = updateBuildScriptDependenciesToApplyFromRemote(buildFileContents, replacement)

        buildFile.text = newBuildFileContents
    }

    public String updateBuildScriptDependenciesToApplyFromRemote(String buildFileContents, String replacement) {
        String newContents = buildFileContents
        int startIndex = buildFileContents.indexOf(START_BUILDSCRIPT_DEPENDENCY)
        int endIndex = buildFileContents.indexOf(END_BUILDSCRIPT_DEPENDENCY)
        if (startIndex > -1 && endIndex > -1) {
            newContents = buildFileContents[0..startIndex - 1] + replacement + buildFileContents[endIndex + END_BUILDSCRIPT_DEPENDENCY.length()..-1]
        }
        return newContents
    }

    public String getBuildScriptDependencyLinePattern() {
        return "apply\\sfrom:\\s[\"\']?.*buildscript-dependencies.gradle[\"\']?,\\sto:\\sbuildscript"
    }

    public String getVersionLinePattern(String currentVersion) {
        return "version\\s*=\\s*[\"\']?${currentVersion}[\"\']?"
    }

    public String getNewVersionLine(String version) {
        return "version = '${version}'"
    }

}
