package com.awscourse.filesmanagementsystem.domain.labelassignment.control;

import com.awscourse.filesmanagementsystem.domain.auditedobject.ObjectState;
import com.awscourse.filesmanagementsystem.domain.labelassignment.entity.LabelAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LabelAssignmentService {

    private final LabelAssignmentRepository labelAssignmentRepository;

    public void removeLabelAssignmentsForLabels(Collection<Long> labelIds) {
        List<LabelAssignment> labelAssignments = labelAssignmentRepository.findAllByLabelIdIn(labelIds);
        labelAssignments.forEach(labelAssignment -> labelAssignment.setObjectState(ObjectState.REMOVED));
    }

}
