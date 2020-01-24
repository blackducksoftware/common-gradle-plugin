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
package com.synopsys.integration

import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

class VersionUtility {
    String calculateReleaseVersion(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        version = StringUtils.removeEnd(version, '-SNAPSHOT')
        version = RegExUtils.removePattern(version, '-SIGQA.*')
        return version
    }

    String calculateNextQAVersion(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        if (StringUtils.isNotBlank(version)) {
            version = StringUtils.removeEnd(version, '-SNAPSHOT')
            if (StringUtils.contains(version, '-SIGQA')) {
                String finalQAVersionPiece = StringUtils.substringAfterLast(version, '-SIGQA')
                String newVersion = StringUtils.substringBeforeLast(version, '-SIGQA')

                Matcher matcher = Pattern.compile("\\d+").matcher(finalQAVersionPiece)
                matcher.find()
                String qaVersionNumber = matcher.group()
                String nextVersion = String.valueOf(Integer.valueOf(qaVersionNumber) + 1)

                finalQAVersionPiece = finalQAVersionPiece.replaceFirst(qaVersionNumber, String.valueOf(nextVersion))

                version = newVersion + '-SIGQA' + finalQAVersionPiece
            } else {
                version += '-SIGQA1'
            }
        }
        return version
    }

    String calculateNextSnapshot(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        if (StringUtils.isNotBlank(version) && !StringUtils.endsWith(version, '-SNAPSHOT')) {
            if (StringUtils.contains(version, '-SIGQA')) {
                version = calculateNextQAVersion(version)
                version += '-SNAPSHOT'
            } else {
                Matcher matcher = Pattern.compile('(\\d+\\.)(\\d+\\.)(\\d+)((\\.\\d+){0,1})').matcher(version)
                if (matcher.find()) {
                    String originalVersion = matcher.group()

                    String finalVersionNumber
                    int max = matcher.groupCount() - 1
                    for (group in 0..max) {
                        String numberInGroup = matcher.group(group)
                        if (StringUtils.isNotBlank(numberInGroup)) {
                            if (numberInGroup.contains('.')) {
                                finalVersionNumber = StringUtils.remove(numberInGroup, '.')
                            } else {
                                finalVersionNumber = numberInGroup
                            }
                        }
                    }

                    Integer nextVersion = Integer.valueOf(finalVersionNumber) + 1

                    String newVersion = StringUtils.removeEnd(originalVersion, finalVersionNumber)
                    newVersion += nextVersion

                    version = version.replaceFirst(originalVersion, newVersion) + '-SNAPSHOT'
                } else {
                    version += '-SNAPSHOT'
                }
            }
        }
        return version
    }

}
