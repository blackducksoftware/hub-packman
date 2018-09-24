package com.blackducksoftware.integration.hub.detect.bomtool.packagist;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.detect.bomtool.BomToolType;
import com.blackducksoftware.integration.hub.detect.configuration.DetectTypedConfiguration;
import com.blackducksoftware.integration.hub.detect.testutils.DependencyGraphResourceTestUtil;
import com.blackducksoftware.integration.hub.detect.testutils.TestUtil;
import com.synopsys.integration.hub.bdio.model.externalid.ExternalIdFactory;

public class PackagistTest {
    private final TestUtil testUtil = new TestUtil();

    @Test
    public void packagistParserTest() throws IOException {
        final DetectTypedConfiguration detectTypedConfiguration = Mockito.mock(DetectTypedConfiguration.class);
        Mockito.when(detectTypedConfiguration.getDetectPackagistIncludeDevDependencies()).thenReturn(true);

        final PackagistParser packagistParser = new PackagistParser(new ExternalIdFactory(), detectTypedConfiguration);

        final String composerLockText = testUtil.getResourceAsUTF8String("/packagist/composer.lock");
        final String composerJsonText = testUtil.getResourceAsUTF8String("/packagist/composer.json");
        final PackagistParseResult result = packagistParser.getDependencyGraphFromProject(BomToolType.COMPOSER_LOCK, "source", composerJsonText, composerLockText);

        Assert.assertEquals(result.projectName, "clue/graph-composer");
        Assert.assertEquals(result.projectVersion, "1.0.0");

        DependencyGraphResourceTestUtil.assertGraph("/packagist/PackagistTestDependencyNode_graph.json", result.codeLocation.getDependencyGraph());
    }
}
