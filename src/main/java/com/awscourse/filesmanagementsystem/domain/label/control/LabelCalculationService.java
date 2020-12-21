package com.awscourse.filesmanagementsystem.domain.label.control;

import com.awscourse.filesmanagementsystem.client.labeldetection.LabelDetectionClient;
import com.awscourse.filesmanagementsystem.domain.label.entity.LabelCalculationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class LabelCalculationService {

    private final LabelDetectionClient labelDetectionClient;

    public Map<URI, List<LabelCalculationResult>> calculateLabelsForResources(Collection<URI> urls, int maxLabelsPerResource, float minConfidence) {
        return labelDetectionClient.getDetectedLabelsByUrl(urls, maxLabelsPerResource, minConfidence);
    }

}
