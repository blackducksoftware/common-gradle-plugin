/*
 * common-gradle-plugin
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.utility

class BuildFileUtility {
    public static final String CGP_VERSION_APPLY_FROM_LINE = "apply from: 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-cgp-version.gradle'"
    public static final String CGP_VERSION_APPLY_FROM_PATTERN = "apply\\sfrom:\\s[\"\']?.*buildscript-cgp-version.gradle[\"\']?"
    public static final String CGP_VERSION_REMOTE_LINE = "project.ext { cgpVersion = '"
    public static final String CGP_VERSION_REMOTE_PATTERN = "project.ext\\s\\{\\scgpVersion\\s=\\s[\"\']?.*[\"\']?\\s\\}"

    void updateVersion(File buildFile, String currentVersion, String newVersion, String releaseType) {
        println "Updating version within ${buildFile} to a ${releaseType} version"
        println "Current version ${currentVersion}"
        println "New ${releaseType} version ${newVersion}"

        String currentVersionPattern = getVersionLinePattern(currentVersion)
        String newVersionPattern = getNewVersionLine(newVersion)

        buildFile.text = buildFile.text.replaceAll(currentVersionPattern, newVersionPattern)
    }

    void updateBuildScript(File buildSrcBuildFile, File rootProjectBuildFile, String stringContainsPattern, String regExPattern, String replacement) {
        if (buildSrcBuildFile.exists() && buildSrcBuildFile.text.contains(stringContainsPattern)) {
            doUpdateBuildScript(buildSrcBuildFile, regExPattern, replacement)
        } else if (rootProjectBuildFile.text.contains(stringContainsPattern)) {
            doUpdateBuildScript(rootProjectBuildFile, regExPattern, replacement)
        } else {
            println "String not found in buildSrc/build.gradle or build.gradle, not performing search & replace of string: ${stringContainsPattern}"
        }
    }

    private void doUpdateBuildScript(File buildFile, String regExPattern, String replacement) {
        println "Updating ${buildFile} to ${replacement}"
        buildFile.text = buildFile.text.replaceAll(regExPattern, replacement)
    }

    private String getVersionLinePattern(String currentVersion) {
        return "version\\s*=\\s*[\"\']?${currentVersion}[\"\']?"
    }

    private String getNewVersionLine(String version) {
        return "version = '${version}'"
    }

}
