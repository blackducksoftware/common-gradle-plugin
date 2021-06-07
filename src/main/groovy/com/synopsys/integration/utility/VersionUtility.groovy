/*
 * common-gradle-plugin
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */

package com.synopsys.integration.utility

import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

public class VersionUtility {
    public static String SUFFIX_SNAPSHOT = '-SNAPSHOT'
    public static String SUFFIX_SIGQA = '-SIGQA'
    public static String VERSION_PATTERN = '(\\d+\\.)(\\d+\\.)(\\d+)((\\.\\d+)*)'

    public String calculateReleaseVersion(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        version = StringUtils.removeEnd(version, SUFFIX_SNAPSHOT)
        version = RegExUtils.removePattern(version, SUFFIX_SIGQA + '[0-9]+')
        return version
    }

    public String calculateNextQAVersion(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        if (StringUtils.isNotBlank(version)) {
            if (!StringUtils.contains(version, SUFFIX_SIGQA) && StringUtils.endsWith(version, SUFFIX_SNAPSHOT)) {
                version = StringUtils.removeEnd(version, SUFFIX_SNAPSHOT)
                version += SUFFIX_SIGQA + '1'
            } else if (StringUtils.contains(version, SUFFIX_SIGQA) && StringUtils.endsWith(version, SUFFIX_SNAPSHOT)) {
                version = StringUtils.removeEnd(version, SUFFIX_SNAPSHOT)
            } else if (StringUtils.contains(version, SUFFIX_SIGQA)) {
                version = StringUtils.removeEnd(version, SUFFIX_SNAPSHOT)
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

    public String calculateNextSnapshot(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        if (StringUtils.isNotBlank(version) && !StringUtils.endsWith(version, SUFFIX_SNAPSHOT)) {
            if (StringUtils.contains(version, SUFFIX_SIGQA)) {
                version = calculateNextQAVersion(version)
                version += SUFFIX_SNAPSHOT
            } else {
                Matcher matcher = Pattern.compile(VERSION_PATTERN).matcher(version)
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
