package com.awscourse.filesmanagementsystem.domain.label.boundary;

import com.awscourse.filesmanagementsystem.api.common.ResourceDTO;
import com.awscourse.filesmanagementsystem.api.label.LabelDTO;
import com.awscourse.filesmanagementsystem.api.label.LabelDetailsDTO;
import com.awscourse.filesmanagementsystem.api.label.LabelSuggestionDTO;
import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import com.awscourse.filesmanagementsystem.domain.label.entity.LabelCalculationResult;
import com.awscourse.filesmanagementsystem.domain.user.boundary.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.IDS_PATH;

@Service
@RequiredArgsConstructor
public class LabelMapper {

    private final UserMapper userMapper;

    public List<LabelDetailsDTO> mapToLabelDetailDTOs(Collection<Label> labels) {
        return labels.stream()
                .map(this::mapToLabelDetailDTO)
                .collect(Collectors.toList());
    }

    public LabelDetailsDTO mapToLabelDetailDTO(Label label) {
        if (label == null) {
            return null;
        }
        return LabelDetailsDTO.builder()
                .id(label.getId())
                .name(label.getName())
                .description(label.getDescription())
                .labelType(label.getLabelType())
                .createdAt(label.getCreatedAt())
                .createdBy(userMapper.mapToUserDTO(label.getCreatedBy()))
                .lastModifiedAt(label.getUpdatedAt())
                .lastModifiedBy(userMapper.mapToUserDTO(label.getUpdatedBy()))
                .build();
    }

    public List<Label> mapToLabels(Collection<LabelDTO> labelDTOs) {
        return labelDTOs.stream()
                .map(this::mapToLabel)
                .collect(Collectors.toList());
    }

    public Label mapToLabel(LabelDTO labelDTO) {
        if (labelDTO == null) {
            return null;
        }
        return new Label(labelDTO.getId(), labelDTO.getName(), labelDTO.getDescription(), labelDTO.getLabelType());
    }

    public List<ResourceDTO> mapToResourceDTOs(Collection<Label> labels) {
        return labels.stream()
                .map(this::mapToResourceDTO)
                .collect(Collectors.toList());
    }

    public ResourceDTO mapToResourceDTO(Label label) {
        if (label == null) {
            return null;
        }
        return ResourceDTO.builder()
                .id(label.getId())
                .identifier(label.getName())
                .uri(getLabelUri(label))
                .build();
    }

    private URI getLabelUri(Label label) {
        if (label == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(LabelController.LABEL_RESOURCE)
                .path(IDS_PATH)
                .buildAndExpand(label.getId())
                .toUri();
    }

    public List<LabelSuggestionDTO> mapToLabelSuggestionDTOs(Collection<LabelCalculationResult> labelCalculationResults) {
        return labelCalculationResults.stream()
                .map(this::mapToLabelSuggestionDTO)
                .collect(Collectors.toList());
    }

    public LabelSuggestionDTO mapToLabelSuggestionDTO(LabelCalculationResult labelCalculationResult) {
        if (labelCalculationResult == null) {
            return null;
        }
        return new LabelSuggestionDTO(labelCalculationResult.getName(), labelCalculationResult.getConfidence());
    }

}
