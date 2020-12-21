package com.awscourse.filesmanagementsystem.domain.labelassignment.control;

import com.awscourse.filesmanagementsystem.domain.file.control.FileService;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.label.control.LabelService;
import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import com.awscourse.filesmanagementsystem.domain.labelassignment.entity.LabelAssignment;
import com.awscourse.filesmanagementsystem.infrastructure.exception.IllegalArgumentAppException;
import com.awscourse.filesmanagementsystem.infrastructure.transform.TransformUtils;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LabelAssignmentService {

    private final LabelAssignmentRepository labelAssignmentRepository;
    private final FileService fileService;
    private final LabelService labelService;

    public List<LabelAssignment> createLabelAssignments(Map<Long, List<Long>> idsOfLabelsToAssignByFileId) {
        List<File> foundFiles = fileService.getFilesByIds(idsOfLabelsToAssignByFileId.keySet());
        List<Label> foundLabels = labelService.getLabelsByIds(TransformUtils.flattenValues(idsOfLabelsToAssignByFileId, ArrayList::new));
        validateBeforeCreate(idsOfLabelsToAssignByFileId, foundFiles, foundLabels);
        Map<File, Set<Label>> labelsToAssignByFile = getLabelsByFile(idsOfLabelsToAssignByFileId, foundFiles, foundLabels);
        List<LabelAssignment> labelAssignmentsToCreate = buildLabelAssignments(labelsToAssignByFile);
        return labelAssignmentRepository.saveAll(labelAssignmentsToCreate);
    }

    private void validateBeforeCreate(Map<Long, List<Long>> idsOfLabelsToAssignByFileId, List<File> foundFiles, List<Label> foundLabels) {
        fileService.validateIfAllFilesExist(idsOfLabelsToAssignByFileId.keySet(), foundFiles);
        labelService.validateIfAllLabelsExists(TransformUtils.flattenValues(idsOfLabelsToAssignByFileId, ArrayList::new), foundLabels);
        validateIfLabelsAreNotAlreadyAssigned(idsOfLabelsToAssignByFileId);
    }

    private void validateIfLabelsAreNotAlreadyAssigned(Map<Long, List<Long>> idsOfLabelsToAssignByFileId) {
        Map<Long, Set<Long>> alreadyAssignedLabelIdsByFileId = getAlreadyAssignedLabelIdsByFileId(idsOfLabelsToAssignByFileId);
        if (thereAreAlreadyAssignedLabels(alreadyAssignedLabelIdsByFileId)) {
            throw new IllegalArgumentAppException(MessageFormat.format("Some of labels are already assigned {0}", alreadyAssignedLabelIdsByFileId));
        }
    }

    private Map<Long, Set<Long>> getAlreadyAssignedLabelIdsByFileId(Map<Long, List<Long>> idsOfLabelsToAssignByFileId) {
        Map<File, Set<Label>> assignedLabelsByFile = getAssignedLabelsByFile(idsOfLabelsToAssignByFileId.keySet());
        Map<Long, Set<Long>> assignedLabelIdsByFileId = TransformUtils.transformMap(assignedLabelsByFile, File::getId, Label::getId, HashSet::new);
        return assignedLabelIdsByFileId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Sets.intersection(entry.getValue(), new HashSet<>(idsOfLabelsToAssignByFileId.get(entry.getKey())))));
    }

    private Map<File, Set<Label>> getAssignedLabelsByFile(Collection<Long> fileIds) {
        List<LabelAssignment> labelAssignments = labelAssignmentRepository.findAllByFileIdIn(fileIds);
        return labelAssignments.stream()
                .collect(Collectors.groupingBy(LabelAssignment::getFile, Collectors.mapping(LabelAssignment::getLabel, Collectors.toSet())));
    }

    private boolean thereAreAlreadyAssignedLabels(Map<Long, Set<Long>> alreadyAssignedLabelIdsByFileId) {
        return alreadyAssignedLabelIdsByFileId.entrySet().stream()
                .anyMatch(fileIdAndAlreadyAssignedLabelIds -> !fileIdAndAlreadyAssignedLabelIds.getValue().isEmpty());
    }

    private Map<File, Set<Label>> getLabelsByFile(Map<Long, List<Long>> labelIdsByFileId, Collection<File> files, Collection<Label> labels) {
        Map<Long, File> foundFilesById = TransformUtils.transformToMap(files, File::getId, Function.identity());
        Map<Long, Label> foundLabelsById = TransformUtils.transformToMap(labels, Label::getId, Function.identity());
        return TransformUtils.transformMap(labelIdsByFileId, foundFilesById::get, foundLabelsById::get, HashSet::new);
    }

    private List<LabelAssignment> buildLabelAssignments(Map<File, Set<Label>> labelsToAssignByFile) {
        return labelsToAssignByFile.entrySet().stream()
                .map(fileAndLabels -> buildLabelAssignmentsForFile(fileAndLabels.getKey(), fileAndLabels.getValue()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<LabelAssignment> buildLabelAssignmentsForFile(File file, Set<Label> labelsToAssign) {
        return labelsToAssign.stream()
                .map(label -> new LabelAssignment(label, file))
                .collect(Collectors.toList());
    }

    public void deleteLabelAssignments(Map<Long, List<Long>> idsOfLabelsToUnassignByFileId) {
        List<File> foundFiles = fileService.getFilesByIds(idsOfLabelsToUnassignByFileId.keySet());
        List<Label> foundLabels = labelService.getLabelsByIds(TransformUtils.flattenValues(idsOfLabelsToUnassignByFileId, ArrayList::new));
        validateBeforeDelete(idsOfLabelsToUnassignByFileId, foundFiles, foundLabels);
        idsOfLabelsToUnassignByFileId.forEach(labelAssignmentRepository::deleteAllByFileIdAndLabelIdIn);
    }

    private void validateBeforeDelete(Map<Long, List<Long>> idsOfLabelsToUnassignByFileId, List<File> foundFiles, List<Label> foundLabels) {
        fileService.validateIfAllFilesExist(idsOfLabelsToUnassignByFileId.keySet(), foundFiles);
        labelService.validateIfAllLabelsExists(TransformUtils.flattenValues(idsOfLabelsToUnassignByFileId, ArrayList::new), foundLabels);
    }

    public void deleteAllAssignmentsForLabels(Collection<Long> labelIds) {
        labelAssignmentRepository.deleteAllByLabelIdIn(labelIds);
    }

}
