package com.awscourse.filesmanagementsystem.client.labeldetection;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.awscourse.filesmanagementsystem.domain.label.entity.LabelCalculationResult;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AwsRecognitionClient implements LabelDetectionClient {

    private static final Set<String> SUPPORTED_FORMATS = Set.of("jpg", "jpeg", "png");
    private static final String EXTENSION_SEPARATOR = ".";
    private static final int LEADING_SLASH_OFFSET = 1;
    private final AmazonRekognition amazonRekognition;

    public AwsRecognitionClient(AWSCredentialsProvider awsCredentialsProvider, RegionProvider regionProvider) {
        this.amazonRekognition = AmazonRekognitionClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion(regionProvider.getRegion().getName())
                .build();
    }

    @Override
    public Map<URI, List<LabelCalculationResult>> detectLabels(Collection<URI> urls, int maxLabelsPerResource, float minConfidence) {
        return urls.stream()
                .collect(Collectors.toMap(Function.identity(),  url -> detectLabelsForResource(url, maxLabelsPerResource, minConfidence)));
    }

    private List<LabelCalculationResult> detectLabelsForResource(URI uri, int maxLabels, float minConfidence) {
        if (!isLabelDetectionSupportedForResourceType(uri)) {
            return Collections.emptyList();
        }
        return getLabelsForImage(uri, maxLabels, minConfidence).stream()
                .map(this::mapToLabelCalculationResult)
                .collect(Collectors.toList());
    }

    private boolean isLabelDetectionSupportedForResourceType(URI url) {
        return SUPPORTED_FORMATS.contains(getExtensionFromUrl(url));
    }

    private String getExtensionFromUrl(URI url) {
        String stringUrl = url.toString();
        int extensionStartIndex = stringUrl.lastIndexOf(EXTENSION_SEPARATOR) + 1;
        return extensionStartIndex > 0 ? stringUrl.substring(extensionStartIndex).toLowerCase() : "";
    }

    public LabelCalculationResult mapToLabelCalculationResult(Label label) {
        return new LabelCalculationResult(label.getName(), label.getConfidence());
    }

    public List<Label> getLabelsForImage(URI url, int maxLabels, float minConfidence) {
        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(getResource(url))
                .withMaxLabels(maxLabels)
                .withMinConfidence(minConfidence);
        return amazonRekognition.detectLabels(request).getLabels();
    }

    private Image getResource(URI url) {
        return new Image()
                .withS3Object(new com.amazonaws.services.rekognition.model.S3Object()
                        .withName(getResourcePath(url))
                        .withBucket(url.getHost()));
    }

    private String getResourcePath(URI url) {
        return url.getPath().substring(LEADING_SLASH_OFFSET);
    }

}
