/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.detect.bomtool.pear

import org.junit.Assert
import org.junit.Test

import com.blackducksoftware.integration.hub.detect.DetectConfiguration
import com.blackducksoftware.integration.hub.detect.DetectProperties
import com.blackducksoftware.integration.hub.detect.testutils.TestUtil

class PearDependencyTest {
    private PearDependencyFinder pearDependencyFinder = new PearDependencyFinder()
    private TestUtil testUtil = new TestUtil()

    @Test
    public void findDependencyNamesTest() {
        DetectProperties detectProperties = new DetectProperties()
        detectProperties.pearNotRequiredDependencies = false
        DetectConfiguration detectConfiguration = new DetectConfiguration()
        detectConfiguration.detectProperties = detectProperties
        pearDependencyFinder.detectConfiguration = detectConfiguration

        def dependenciesList = testUtil.getResourceAsUTF8String('/pear/package-xml-dependencies-list.txt')

        Set<String> actual = pearDependencyFinder.findDependencyNames(dependenciesList)
        Set<String> expected = [
            'Horde_Exception',
            'Horde_Util',
            'Horde_Xml_Element'
        ]

        Assert.assertEquals(expected, actual)
    }

    @Test
    public void createPearDependencyNodeFromListTest() {
        def installedPackages = testUtil.getResourceAsUTF8String('/pear/installed-packages.txt')

        Set<String> dependencyNames = [
            'Horde_Exception',
            'Horde_Util',
            'Horde_Xml_Element'
        ]
        def actual = pearDependencyFinder.createPearDependencyNodeFromList(installedPackages, dependencyNames)
        def expected = testUtil.getResourceAsUTF8String('/pear/dependency-node-list.txt')

        Assert.assertTrue(actual.toString().equals(expected))
    }
}
