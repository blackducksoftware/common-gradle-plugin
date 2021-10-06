package com.synopsys.integration.utility

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class VersionUtilityTest {
    @Test
    void calculateReleaseVersionTest() {
        VersionUtility versionUtility = new VersionUtility()
        assertEquals('', versionUtility.calculateReleaseVersion(null))
        assertEquals('', versionUtility.calculateReleaseVersion(''))
        assertEquals('1.1.1', versionUtility.calculateReleaseVersion('1.1.1'))
        assertEquals('6.1.0', versionUtility.calculateReleaseVersion('6.1.0'))
        assertEquals('6.1.0', versionUtility.calculateReleaseVersion('6.1.0-SNAPSHOT'))
        assertEquals('6.1.0', versionUtility.calculateReleaseVersion('6.1.0-SIGQA1'))
        assertEquals('6.1.0', versionUtility.calculateReleaseVersion('6.1.0-SIGQA2-SNAPSHOT'))
        assertEquals('6.1.0', versionUtility.calculateReleaseVersion('6.1.0-SIGQA59932'))

        assertEquals('6.1.0-RC', versionUtility.calculateReleaseVersion('6.1.0-RC'))
        assertEquals('6.1.0_RC', versionUtility.calculateReleaseVersion('6.1.0_RC'))
        assertEquals('6.1.0-RC1', versionUtility.calculateReleaseVersion('6.1.0-RC1'))
        assertEquals('6.1.0-RC1.4', versionUtility.calculateReleaseVersion('6.1.0-RC1.4'))
        assertEquals('6.1.0_RC1.4', versionUtility.calculateReleaseVersion('6.1.0_RC1.4'))
        assertEquals('6.1.0+RC1.4', versionUtility.calculateReleaseVersion('6.1.0+RC1.4'))
        assertEquals('1.1.1+RC1.1.1', versionUtility.calculateReleaseVersion('1.1.1+RC1.1.1'))

        assertEquals('6.1.0-ALPHA3', versionUtility.calculateReleaseVersion('6.1.0-ALPHA3-SIGQA34-SNAPSHOT'))
        assertEquals('6.1.0-ALPHA3.5', versionUtility.calculateReleaseVersion('6.1.0-ALPHA3.5-SIGQA34-SNAPSHOT'))
        assertEquals('6.1.0-BETA', versionUtility.calculateReleaseVersion('6.1.0-BETA-SNAPSHOT'))

        assertEquals('2020.1.0', versionUtility.calculateReleaseVersion('2020.1.0-SIGQA1-SNAPSHOT'))
        assertEquals('2020.1.0.88', versionUtility.calculateReleaseVersion('2020.1.0.88-SIGQA1-SNAPSHOT'))
        assertEquals('2020.1.0.88', versionUtility.calculateReleaseVersion('2020.1.0.88'))
        assertEquals('jaloja', versionUtility.calculateReleaseVersion('jaloja'))

        assertEquals('junk.1.1.1', versionUtility.calculateReleaseVersion('junk.1.1.1-SNAPSHOT'))
        assertEquals('junk.1.1.1', versionUtility.calculateReleaseVersion('junk.1.1.1-SIGQA34-SNAPSHOT'))

        assertEquals('real_junk+stuff.1.1.1', versionUtility.calculateReleaseVersion('real_junk+stuff.1.1.1-SNAPSHOT'))
        assertEquals('real_junk+stuff.1.1.1', versionUtility.calculateReleaseVersion('real_junk+stuff.1.1.1-SIGQA34-SNAPSHOT'))
        assertEquals('real_junk+stuff.1.1.1', versionUtility.calculateReleaseVersion('real_junk+stuff.1.1.1'))
        assertEquals('real_junk+stuff.1.1.1', versionUtility.calculateReleaseVersion('real_junk+stuff.1.1.1-SIGQA34'))
    }

    @Test
    void calculateNextQAVersionTest() {
        VersionUtility versionUtility = new VersionUtility()
        assertEquals('', versionUtility.calculateNextQAVersion(null))
        assertEquals('', versionUtility.calculateNextQAVersion(''))
        assertEquals('1.1.1-SIGQA1', versionUtility.calculateNextQAVersion('1.1.1'))
        assertEquals('6.1.0-SIGQA1', versionUtility.calculateNextQAVersion('6.1.0'))
        assertEquals('6.1.0-SIGQA1', versionUtility.calculateNextQAVersion('6.1.0-SNAPSHOT'))
        assertEquals('6.1.0-SIGQA2', versionUtility.calculateNextQAVersion('6.1.0-SIGQA1'))
        assertEquals('6.1.0-SIGQA2', versionUtility.calculateNextQAVersion('6.1.0-SIGQA2-SNAPSHOT'))
        assertEquals('6.1.0-SIGQA59933', versionUtility.calculateNextQAVersion('6.1.0-SIGQA59932'))

        assertEquals('6.1.0-RC-SIGQA1', versionUtility.calculateNextQAVersion('6.1.0-RC'))
        assertEquals('6.1.0_RC-SIGQA1', versionUtility.calculateNextQAVersion('6.1.0_RC'))
        assertEquals('6.1.0-RC1-SIGQA1', versionUtility.calculateNextQAVersion('6.1.0-RC1'))
        assertEquals('6.1.0-RC1.4-SIGQA1', versionUtility.calculateNextQAVersion('6.1.0-RC1.4'))
        assertEquals('6.1.0_RC1.4-SIGQA1', versionUtility.calculateNextQAVersion('6.1.0_RC1.4'))
        assertEquals('6.1.0+RC1.4-SIGQA1', versionUtility.calculateNextQAVersion('6.1.0+RC1.4'))
        assertEquals('1.1.1+RC1.1.1-SIGQA1', versionUtility.calculateNextQAVersion('1.1.1+RC1.1.1'))

        assertEquals('6.1.0-ALPHA3-SIGQA34', versionUtility.calculateNextQAVersion('6.1.0-ALPHA3-SIGQA34-SNAPSHOT'))
        assertEquals('6.1.0-ALPHA3.5-SIGQA34', versionUtility.calculateNextQAVersion('6.1.0-ALPHA3.5-SIGQA34-SNAPSHOT'))
        assertEquals('6.1.0-BETA-SIGQA1', versionUtility.calculateNextQAVersion('6.1.0-BETA-SNAPSHOT'))

        assertEquals('2020.1.0-SIGQA1', versionUtility.calculateNextQAVersion('2020.1.0-SIGQA1-SNAPSHOT'))
        assertEquals('2020.1.0.88-SIGQA1', versionUtility.calculateNextQAVersion('2020.1.0.88-SIGQA1-SNAPSHOT'))
        assertEquals('2020.1.0.88-SIGQA1', versionUtility.calculateNextQAVersion('2020.1.0.88'))
        assertEquals('jaloja-SIGQA1', versionUtility.calculateNextQAVersion('jaloja'))

        assertEquals('junk.1.1.1-SIGQA1', versionUtility.calculateNextQAVersion('junk.1.1.1-SNAPSHOT'))
        assertEquals('junk.1.1.1-SIGQA34', versionUtility.calculateNextQAVersion('junk.1.1.1-SIGQA34-SNAPSHOT'))

        assertEquals('real_junk+stuff.1.1.1-SIGQA1', versionUtility.calculateNextQAVersion('real_junk+stuff.1.1.1-SNAPSHOT'))
        assertEquals('real_junk+stuff.1.1.1-SIGQA34', versionUtility.calculateNextQAVersion('real_junk+stuff.1.1.1-SIGQA34-SNAPSHOT'))
        assertEquals('real_junk+stuff.1.1.1-SIGQA1', versionUtility.calculateNextQAVersion('real_junk+stuff.1.1.1'))
        assertEquals('real_junk+stuff.1.1.1-SIGQA35', versionUtility.calculateNextQAVersion('real_junk+stuff.1.1.1-SIGQA34'))
    }

    @Test
    void calculateNextSnapshotTest() {
        VersionUtility versionUtility = new VersionUtility()
        assertEquals('', versionUtility.calculateNextSnapshot(null))
        assertEquals('', versionUtility.calculateNextSnapshot(''))
        assertEquals('1.1.2-SNAPSHOT', versionUtility.calculateNextSnapshot('1.1.1'))
        assertEquals('6.1.1-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0'))
        assertEquals('6.1.0-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-SNAPSHOT'))
        assertEquals('6.1.0-SIGQA2-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-SIGQA1'))
        assertEquals('6.1.0-SIGQA2-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-SIGQA2-SNAPSHOT'))
        assertEquals('6.1.0-SIGQA59933-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-SIGQA59932'))

        assertEquals('6.1.1-RC-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-RC'))
        assertEquals('6.1.1_RC-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0_RC'))
        assertEquals('6.1.1-RC1-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-RC1'))
        assertEquals('6.1.1-RC1.4-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-RC1.4'))
        assertEquals('6.1.1_RC1.4-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0_RC1.4'))
        assertEquals('6.1.1+RC1.4-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0+RC1.4'))
        assertEquals('1.1.2+RC1.1.1-SNAPSHOT', versionUtility.calculateNextSnapshot('1.1.1+RC1.1.1'))

        assertEquals('6.1.0-ALPHA3-SIGQA34-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-ALPHA3-SIGQA34-SNAPSHOT'))
        assertEquals('6.1.0-ALPHA3.5-SIGQA34-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-ALPHA3.5-SIGQA34-SNAPSHOT'))
        assertEquals('6.1.0-BETA-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-BETA-SNAPSHOT'))
        assertEquals('6.1.1-BETA-SNAPSHOT', versionUtility.calculateNextSnapshot('6.1.0-BETA'))

        assertEquals('2020.1.0-SIGQA1-SNAPSHOT', versionUtility.calculateNextSnapshot('2020.1.0-SIGQA1-SNAPSHOT'))
        assertEquals('2020.1.0.88-SIGQA1-SNAPSHOT', versionUtility.calculateNextSnapshot('2020.1.0.88-SIGQA1-SNAPSHOT'))
        assertEquals('2020.1.0.89-SNAPSHOT', versionUtility.calculateNextSnapshot('2020.1.0.88'))
        assertEquals('2020.1.0.4.5.1-SNAPSHOT', versionUtility.calculateNextSnapshot('2020.1.0.4.5.0'))
        assertEquals('2020.1.0.4.5.0.8.4.5-SNAPSHOT', versionUtility.calculateNextSnapshot('2020.1.0.4.5.0.8.4.4'))
        assertEquals('jaloja1-SNAPSHOT', versionUtility.calculateNextSnapshot('jaloja'))

        assertEquals('junk.1.1.1-SNAPSHOT', versionUtility.calculateNextSnapshot('junk.1.1.1-SNAPSHOT'))
        assertEquals('junk.1.1.1-SIGQA34-SNAPSHOT', versionUtility.calculateNextSnapshot('junk.1.1.1-SIGQA34-SNAPSHOT'))

        assertEquals('real_junk+stuff.1.1.1-SNAPSHOT', versionUtility.calculateNextSnapshot('real_junk+stuff.1.1.1-SNAPSHOT'))
        assertEquals('real_junk+stuff.1.1.1-SIGQA34-SNAPSHOT', versionUtility.calculateNextSnapshot('real_junk+stuff.1.1.1-SIGQA34-SNAPSHOT'))
        assertEquals('real_junk+stuff.1.1.2-SNAPSHOT', versionUtility.calculateNextSnapshot('real_junk+stuff.1.1.1'))
        assertEquals('real_junk+stuff.1.1.1-SIGQA35-SNAPSHOT', versionUtility.calculateNextSnapshot('real_junk+stuff.1.1.1-SIGQA34'))
    }

    @Test
    void expectedVersionFlowTest() {
        VersionUtility versionUtility = new VersionUtility()
        String version = '4.7.3-SNAPSHOT'
        version = versionUtility.calculateNextQAVersion(version)
        assertEquals('4.7.3-SIGQA1', version)
        version = versionUtility.calculateNextSnapshot(version)
        assertEquals('4.7.3-SIGQA2-SNAPSHOT', version)
        version = versionUtility.calculateNextQAVersion(version)
        assertEquals('4.7.3-SIGQA2', version)
        version = versionUtility.calculateNextSnapshot(version)
        assertEquals('4.7.3-SIGQA3-SNAPSHOT', version)
        version = versionUtility.calculateReleaseVersion(version)
        assertEquals('4.7.3', version)
        version = versionUtility.calculateNextSnapshot(version)
        assertEquals('4.7.4-SNAPSHOT', version)
    }

}
