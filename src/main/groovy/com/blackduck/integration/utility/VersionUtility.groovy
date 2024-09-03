package com.blackduck.integration.utility

import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

class VersionUtility {
    public static final String SUFFIX_SNAPSHOT = '-SNAPSHOT'
    public static final String SUFFIX_SIGQA = '-SIGQA'
    public static final String VERSION_PATTERN = '(\\d+\\.)(\\d+\\.)(\\d+)((\\.\\d+)*)'
    public static final String RELEASE_BRANCH_PATTERN = '(\\d+\\.)(\\d+\\.)((\\d+\\.)*)(\\d+|z)'

    String calculateReleaseVersion(String currentVersion) {
        String version = StringUtils.trimToEmpty(currentVersion)
        version = StringUtils.removeEnd(version, SUFFIX_SNAPSHOT)
        version = RegExUtils.removePattern(version, SUFFIX_SIGQA + '[0-9]+')
        return version
    }

    String calculateNextQAVersion(String currentVersion) {
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

    String calculateNextQAVersionDetect(String currentVersion, String runningBranch) {
        String version = StringUtils.trimToEmpty(currentVersion)
        if (StringUtils.isNotBlank(version)) {
            version = removeBranchNameFromVersion(version)
            version = calculateNextQAVersion(version)
        }
        String branchSuffix = findVersionSuffixForBranch(runningBranch)
        return StringUtils.isNotBlank(branchSuffix) ? (version + '-' + branchSuffix) : version
    }

    String calculateNextSnapshot(String currentVersion) {
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

    String findVersionSuffixForBranch(String runningBranch) {
        if (StringUtils.isNotBlank(runningBranch)) {
            runningBranch = StringUtils.replace(runningBranch, "origin/", '')
            if ((runningBranch == 'master' || runningBranch =~ ("^${RELEASE_BRANCH_PATTERN}\$")) == Boolean.FALSE) {
                runningBranch = StringUtils.replace(runningBranch, "dev/", '')
                runningBranch = StringUtils.replace(runningBranch, "feature/", '')
                return RegExUtils.replaceAll(runningBranch, '/', '.')
            }
        }
        return ''
    }

    String removeBranchNameFromVersion(String currentVersion) {
        if (currentVersion.endsWith(SUFFIX_SNAPSHOT)) {
            if (currentVersion =~ (SUFFIX_SIGQA + /\d+/))
                return currentVersion.replaceAll(/($SUFFIX_SIGQA\d+).*/, '$1') + SUFFIX_SNAPSHOT
            return currentVersion.replaceAll(/($VERSION_PATTERN).*/, '$1') + SUFFIX_SNAPSHOT
        }
        // if -SNAPSHOT isn't in the end, that means the rest is the branch name. Although -SNAPSHOT should always be at the end.
        if (currentVersion.contains(SUFFIX_SNAPSHOT))
            return currentVersion.replaceAll(/($SUFFIX_SNAPSHOT).*/, '$1')
        // -SNAPSHOT is not present. if -SIGQA[digit] isn't in the end, that means the rest is the branch name
        if (currentVersion =~ (SUFFIX_SIGQA + /\d+/))
            return currentVersion.replaceAll(/($SUFFIX_SIGQA\d+).*/, '$1')
        // if none of the above exists, fetch substring upto the version
        return currentVersion.replaceAll(/($VERSION_PATTERN).*/, '$1')
    }
}
