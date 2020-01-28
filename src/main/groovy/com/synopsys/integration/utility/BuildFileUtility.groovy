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
package com.synopsys.integration.utility

class BuildFileUtility {
    public void updateVersion(File buildFile, String newVersion) {
        String buildFileContents = buildFile.text
        String newBuildFileContents = updateVersion(buildFileContents, newVersion)

        buildFile.text = newBuildFileContents
    }

    public String updateVersion(String buildFileContents, String newVersion) {
        String versionLinePattern = getVersionLinePattern()
        String newVersionLine = getNewVersionLine(newVersion)

        String newContents = buildFileContents.replaceAll(versionLinePattern, newVersionLine)
        return newContents
    }

    public String getVersionLinePattern() {
        return "version\\s*=\\s*[\"\']?${VersionUtility.VERSION_PATTERN}(${VersionUtility.SUFFIX_SIGQA}\\d+)?(${VersionUtility.SUFFIX_SNAPSHOT})?[\"\']?"
    }

    public String getNewVersionLine(String version) {
        return "version = '${version}'"
    }


}
