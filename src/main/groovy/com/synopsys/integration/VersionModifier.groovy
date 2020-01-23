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

class VersionModifier {
    String calculateReleaseVersion(String currentVersion) {
        String version = StringUtils.removeEnd(currentVersion, '-SNAPSHOT')
        version = RegExUtils.removePattern(version, '-SIGQA.*')
        return version
    }

    String calculateNextQAVersion(String currentVersion) {
        String version = StringUtils.removeEnd(currentVersion, '-SNAPSHOT')
        if (StringUtils.contains(version, '-SIGQA')) {
            String lastChar = version[-1..-1]
            Integer nextVersion = Integer.valueOf(lastChar) + 1
            String versionWithoutQAVersion = version[0..-1]
            version = versionWithoutQAVersion + nextVersion
        } else {
            version += '-SIGQA1'
        }
        return version
    }

    String calculateNextSnapshot(String currentVersion) {
        String version = currentVersion
        if (!StringUtils.endsWith(version, '-SNAPSHOT')) {
            if (StringUtils.contains(version, '-SIGQA')) {
                version += '-SNAPSHOT'
            } else {
                String lastChar = version[-1..-1]
                Integer nextVersion = Integer.valueOf(lastChar) + 1
                String versionWithoutQAVersion = version[0..-1]
                version = versionWithoutQAVersion + nextVersion + '-SNAPSHOT'
            }
        }
        return version
    }

}
