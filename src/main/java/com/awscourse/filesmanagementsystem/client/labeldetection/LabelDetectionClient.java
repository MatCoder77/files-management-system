package com.awscourse.filesmanagementsystem.client.labeldetection;

import com.awscourse.filesmanagementsystem.domain.label.entity.LabelCalculationResult;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface LabelDetectionClient {

    Map<URI, List<LabelCalculationResult>> detectLabels(Collection<URI> urls, int maxLabelsPerResource, float minConfidence);

}
