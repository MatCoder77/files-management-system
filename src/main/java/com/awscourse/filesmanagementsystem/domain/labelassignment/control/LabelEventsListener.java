package com.awscourse.filesmanagementsystem.domain.labelassignment.control;

import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import com.awscourse.filesmanagementsystem.infrastructure.event.crud.bulk.BulkDeleteEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LabelEventsListener {

    private final LabelAssignmentService labelAssignmentService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void removeLabelAssignmentsForRemovedLabels(BulkDeleteEvent<Label> event) {
        labelAssignmentService.deleteAllAssignmentsForLabels(getIds(event.getRelatedObjects()));
    }

    private List<Long> getIds(Collection<Label> labels) {
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toList());
    }

}
