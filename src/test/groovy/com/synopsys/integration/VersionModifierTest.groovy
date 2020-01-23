package com.synopsys.integration

import com.synopsys.integration.VersionModifier
import org.junit.jupiter.api.Test

import static org.junit.Assert.assertEquals

class VersionModifierTest {

    @Test
    void calculateReleaseVersionTest() {
        VersionModifier versionModifier = new VersionModifier()
        assertEquals('6.1.0', versionModifier.calculateReleaseVersion('6.1.0-SNAPSHOT'))
        assertEquals('6.1.0', versionModifier.calculateReleaseVersion('6.1.0-SIGQA1'))
        assertEquals('6.1.0', versionModifier.calculateReleaseVersion('6.1.0-SIGQA2'))
        assertEquals('6.1.0', versionModifier.calculateReleaseVersion('6.1.0-SIGQA59932'))

        assertEquals('6.1.0-RC', versionModifier.calculateReleaseVersion('6.1.0-RC'))
        assertEquals('6.1.0-ALPHA', versionModifier.calculateReleaseVersion('6.1.0-ALPHA-SIGQA34-SNAPSHOT'))
        assertEquals('6.1.0-BETA', versionModifier.calculateReleaseVersion('6.1.0-BETA-SNAPSHOT'))

    }
}
