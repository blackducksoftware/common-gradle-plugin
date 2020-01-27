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

import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

class VersionUtility {
    public static String SUFFIX_SNAPSHOT = '-SNAPSHOT'
    public static String SUFFIX_SIGQA = '-SIGQA'

    String calculateReleaseVersion(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        version = StringUtils.removeEnd(version, SUFFIX_SNAPSHOT)
        version = RegExUtils.removePattern(version, SUFFIX_SIGQA + '[0-9]+')
        return version
    }

    String calculateNextQAVersion(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        if (StringUtils.isNotBlank(version)) {
            version = StringUtils.removeEnd(version, SUFFIX_SNAPSHOT)
            if (StringUtils.contains(version, SUFFIX_SIGQA)) {
                String finalQAVersionPiece = StringUtils.substringAfterLast(version, SUFFIX_SIGQA)
                String newVersion = StringUtils.substringBeforeLast(version, SUFFIX_SIGQA)

                Matcher matcher = Pattern.compile("\\d+").matcher(finalQAVersionPiece)
                matcher.find()
                String qaVersionNumber = matcher.group()
                String nextVersion = String.valueOf(Integer.valueOf(qaVersionNumber) + 1)

                finalQAVersionPiece = finalQAVersionPiece.replaceFirst(qaVersionNumber, String.valueOf(nextVersion))

                version = newVersion + SUFFIX_SIGQA + finalQAVersionPiece
            } else {
                version += SUFFIX_SIGQA + '1'
            }
        }
        return version
    }

    String calculateNextSnapshot(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        if (StringUtils.isNotBlank(version) && !StringUtils.endsWith(version, SUFFIX_SNAPSHOT)) {
            if (StringUtils.contains(version, SUFFIX_SIGQA)) {
                version = calculateNextQAVersion(version)
                version += SUFFIX_SNAPSHOT
            } else {
                Matcher matcher = Pattern.compile('(\\d+\\.)(\\d+\\.)(\\d+)((\\.\\d+)*)').matcher(version)
                if (matcher.find()) {
                    String originalVersion = matcher.group()

                    String finalVersionNumber
                    int max = matcher.groupCount() - 1
                    for (group in 0..max) {
                        String numberInGroup = matcher.group(group)
                        if (StringUtils.isNotBlank(numberInGroup)) {
                            if (numberInGroup.contains('.')) {
                                finalVersionNumber = StringUtils.substringAfterLast(numberInGroup, '.')
                            } else {
                                finalVersionNumber = numberInGroup
                            }
                        }
                    }

                    Integer nextVersion = Integer.valueOf(finalVersionNumber) + 1

                    String newVersion = StringUtils.removeEnd(originalVersion, finalVersionNumber)
                    newVersion += nextVersion

                    version = version.replaceFirst(originalVersion, newVersion) + SUFFIX_SNAPSHOT
                } else {
                    version += '1' + SUFFIX_SNAPSHOT
                }
            }
        }
        return version
    }

}
