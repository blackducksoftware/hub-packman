package com.blackducksoftware.integration.hub.detect.hub;

import com.blackducksoftware.integration.hub.detect.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.model.DetectProject;
import com.blackducksoftware.integration.hub.service.model.ProjectRequestBuilder;

public class DetectProjectRequestBuilder extends ProjectRequestBuilder {

    public DetectProjectRequestBuilder(final DetectConfiguration detectConfiguration, final DetectProject detectResult) {
        setProjectName(detectResult.getProjectName());
        setVersionName(detectResult.getProjectVersion());
        setProjectLevelAdjustments(detectConfiguration.getProjectLevelMatchAdjustments());
        setPhase(detectConfiguration.getProjectVersionPhase());
        setDistribution(detectConfiguration.getProjectVersionDistribution());
        setProjectTier(detectConfiguration.getProjectTier());
        setReleaseComments(detectConfiguration.getProjectVersionNotes());
    }
}