package com.awscourse.filesmanagementsystem.domain.labelassignment.control;

import com.awscourse.filesmanagementsystem.domain.file.boundary.FileBulkDeletedEvent;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.label.boundary.LabelBulkDeletedEvent;
import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LabelAssignmentListener {

    private final LabelAssignmentService labelAssignmentService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void removeLabelAssignmentsForRemovedLabels(LabelBulkDeletedEvent event) {
        labelAssignmentService.deleteAllAssignmentsForLabels(getLabelIds(event.getRelatedObjects()));
    }

    private List<Long> getLabelIds(Collection<Label> labels) {
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toList());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void removeLabelAssignmentsForRemovedFiles(FileBulkDeletedEvent event) {
        labelAssignmentService.deleteAllAssignmentsForFile(getFileIds(event.getRelatedObjects()));
    }

    private List<Long> getFileIds(Collection<File> files) {
        return files.stream()
                .map(File::getId)
                .collect(Collectors.toList());
    }

}
