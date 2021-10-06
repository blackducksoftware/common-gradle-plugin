package com.synopsys.integration.utility

import java.nio.charset.StandardCharsets

class BuildFileUtility {
    private static final String CGP_VERSION_FILE_NAME = 'https://raw.githubusercontent.com/blackducksoftware/integration-resources/master/gradle_common/buildscript-cgp-version.gradle'
    public static final String CGP_VERSION_APPLY_FROM_LINE = "apply from: '${CGP_VERSION_FILE_NAME}'"

    void updateBuildScriptVersion(File buildFile, String newVersion, String releaseType) {
        println "Updating version within ${buildFile} to a ${releaseType} version"
        println "New ${releaseType} version ${newVersion}"

        updateBuildScript("" as File, buildFile, "version = ", "version = '${newVersion}'")
    }

    void updateBuildScriptForRelease(File buildSrcBuildFile, File rootProjectBuildFile) {
        String remoteCgpVersionContents = getRemoteCgpVersionContents()
        updateBuildScript(buildSrcBuildFile, rootProjectBuildFile, CGP_VERSION_APPLY_FROM_LINE, remoteCgpVersionContents)
    }

    void updateBuildScriptForSnapshot(File buildSrcBuildFile, File rootProjectBuildFile) {
        String remoteCgpVersionContents = getRemoteCgpVersionContents()
        String remoteCgpVersionSearchPattern = remoteCgpVersionContents.substring(0, remoteCgpVersionContents.indexOf("'"))
        updateBuildScript(buildSrcBuildFile, rootProjectBuildFile, remoteCgpVersionSearchPattern, CGP_VERSION_APPLY_FROM_LINE)
    }

    private void updateBuildScript(File buildSrcBuildFile, File rootProjectBuildFile, String searchPattern, String replacement) {
        boolean foundSearchPattern = doUpdateBuildScript(buildSrcBuildFile, searchPattern, replacement)

        if (!foundSearchPattern) {
            foundSearchPattern = doUpdateBuildScript(rootProjectBuildFile, searchPattern, replacement)
        }
        if (!foundSearchPattern) {
            println "String not found in buildSrc/build.gradle or build.gradle, not performing search & replace of string: ${searchPattern}"
        }
    }

    private boolean doUpdateBuildScript(File buildFile, String searchPattern, String replacement) {
        if (buildFile.exists()) {
            List<String> buildFileContents = buildFile.readLines()
            for (int i = 0; i < buildFileContents.size(); i++) {
                String line = buildFileContents.get(i)
                if (line.trim().startsWith(searchPattern)) {
                    String spacing = line.substring(0, line.indexOf(line.trim()))
                    buildFileContents.set(i, spacing + replacement)
                    buildFile.text = buildFileContents.join("\n")
                    return true
                }
            }
        }
        return false
    }

    static String getRemoteCgpVersionContents() {
        URL url = new URL(CGP_VERSION_FILE_NAME)
        return url.getText(StandardCharsets.UTF_8.name()).trim()
    }

}