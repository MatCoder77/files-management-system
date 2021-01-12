package com.awscourse.filesmanagementsystem.domain.label.control;

import com.awscourse.filesmanagementsystem.domain.auditedobject.ObjectState;
import com.awscourse.filesmanagementsystem.domain.label.boundary.LabelBulkDeletedEvent;
import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import com.awscourse.filesmanagementsystem.infrastructure.transform.TransformUtils;
import com.awscourse.filesmanagementsystem.infrastructure.exception.IllegalArgumentAppException;
import com.awscourse.filesmanagementsystem.infrastructure.exception.ExceptionUtils;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<Label> getLabelsByIds(Collection<Long> ids) {
        return labelRepository.findAllById(ids);
    }

    public List<Label> getLabelsCreatedByUser(Long userId) {
        return labelRepository.findAllByCreatedById(userId);
    }

    public List<Label> createLabels(Collection<Label> labels) {
        prepareBeforeCreate(labels);
        validateBeforeCreate(labels);
        return labelRepository.saveAll(labels);
    }

    private void prepareBeforeCreate(Collection<Label> labels) {
        labels.forEach(this::prepareBeforeCreate);
    }

    private void prepareBeforeCreate(Label label) {
        label.setId(null);
        label.setObjectState(ObjectState.ACTIVE);
    }

    private void validateBeforeCreate(Collection<Label> labels) {
        validateNameUniqueness(labels);
    }

    private void validateNameUniqueness(Collection<Label> labels) {
        validateIfThereAreNoNameDuplicatesAmongSubmittedLabels(labels);
        validateIfThereAreNoNameDuplicatesAmongExistingLabels(labels);
    }

    private void validateIfThereAreNoNameDuplicatesAmongSubmittedLabels(Collection<Label> labels) {
        List<String> labelNames = getNames(labels);
        if (labelNames.size() != new HashSet<>(labelNames).size()) {
            throw new IllegalArgumentAppException("Labels to create have non-unique names!");
        }
    }

    private List<String> getNames(Collection<Label> labels) {
        return TransformUtils.transformToList(labels, Label::getName);
    }

    private void validateIfThereAreNoNameDuplicatesAmongExistingLabels(Collection<Label> labels) {
        List<Label> foundDuplicates = findDuplicatedLabelsAmongExistingLabels(labels);
        if (!foundDuplicates.isEmpty()) {
            throw new IllegalArgumentAppException(MessageFormat.format("There are existing labels with given names {0}", getNames(foundDuplicates)));
        }
    }

    private List<Label> findDuplicatedLabelsAmongExistingLabels(Collection<Label> labels) {
        Set<Long> labelIds = getNonNullUniqueIds(labels);
        if (labelIds.isEmpty()) {
            return labelRepository.findAllByNameIn(getNames(labels));
        }
        return labelRepository.findAllByNameInAndIdNotIn(getNames(labels), labelIds);
    }

    private Set<Long> getNonNullUniqueIds(Collection<Label> labels) {
        return labels.stream()
                .map(Label::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void updateLabels(Collection<Label> updatedLabels, Long userId) {
        List<Label> foundLabels = labelRepository.findAllById(getNonNullUniqueIds(updatedLabels));
        validateBeforeUpdate(foundLabels, updatedLabels, userId);
        updateLabels(foundLabels, updatedLabels);
    }

    private void validateBeforeUpdate(Collection<Label> existingLabels, Collection<Label> updatedLabels, Long userId) {
        validateIfAllLabelsHaveUniqueId(updatedLabels);
        validateIfAllLabelsExists(getNonNullUniqueIds(updatedLabels), existingLabels);
        validateNameUniqueness(updatedLabels);
        validatePermissions(existingLabels, userId);
    }

    private void validateIfAllLabelsHaveUniqueId(Collection<Label> labels) {
        if (getNonNullUniqueIds(labels).size() != labels.size()) {
            throw new IllegalArgumentAppException("There are some labels without id or ids are not unique!");
        }
    }

    public void validateIfAllLabelsExists(Collection<Long> ids, Collection<Label> foundLabels) {
        Set<Long> idsOfNonExistingEducationalEffects = getIdsOfNonExistingLabels(ids, foundLabels);
        if (!idsOfNonExistingEducationalEffects.isEmpty()) {
            throw ExceptionUtils.getObjectNotFoundException(Label.class, idsOfNonExistingEducationalEffects);
        }
    }

    private Set<Long> getIdsOfNonExistingLabels(Collection<Long> idsOfLabelsToRemove, Collection<Label> foundLabels) {
        return Sets.difference(new HashSet<>(idsOfLabelsToRemove), getNonNullUniqueIds(foundLabels));
    }

    private void validatePermissions(Collection<Label> labels, Long userId) {
        List<Long> labelsWithoutPermissions = getLabelsWithoutPermissions(labels, userId);
        if (!labelsWithoutPermissions.isEmpty()) {
            throw new IllegalArgumentAppException(MessageFormat.format("No permissions to perform operations for labels {0}", StringUtils.join(labelsWithoutPermissions)));
        }
    }

    private List<Long> getLabelsWithoutPermissions(Collection<Label> labels, Long userId) {
        return labels.stream()
                .filter(label -> isNotLabelCreator(label, userId))
                .map(Label::getId)
                .collect(Collectors.toList());
    }

    private boolean isNotLabelCreator(Label label, Long userId) {
        return !label.getCreatedBy().getId().equals(userId);
    }

    private void updateLabels(Collection<Label> existingLabels, Collection<Label> updatedLabels) {
        Map<Long, Label> updatedLabelsById = getLabelsById(updatedLabels);
        existingLabels.forEach(existingLabel -> updateLabel(existingLabel, updatedLabelsById.get(existingLabel.getId())));
    }

    private Map<Long, Label> getLabelsById(Collection<Label> labels) {
        return labels.stream()
                .collect(Collectors.toMap(Label::getId, Function.identity()));
    }

    private void updateLabel(Label existingLabel, Label updatedLabel) {
        existingLabel.setName(updatedLabel.getName());
        existingLabel.setDescription(updatedLabel.getDescription());
        existingLabel.setLabelType(updatedLabel.getLabelType());
    }

    public void deleteLabels(Collection<Long> ids, Long userId) {
        List<Label> foundLabels = labelRepository.findAllById(ids);
        validateBeforeDelete(ids, foundLabels, userId);
        foundLabels.forEach(label -> label.setObjectState(ObjectState.REMOVED));
        publishLabelBulkDeletedEvent(foundLabels);
    }

    private void validateBeforeDelete(Collection<Long> ids, Collection<Label> labels, Long userId) {
        validateIfAllLabelsExists(ids, labels);
        validatePermissions(labels, userId);
    }

    private void publishLabelBulkDeletedEvent(List<Label> removedLabels) {
        eventPublisher.publishEvent(new LabelBulkDeletedEvent(this, removedLabels));
    }

    public List<Label> getLabelsByNames(Collection<String> names) {
        return labelRepository.findAllByNameIn(names);
    }

}
