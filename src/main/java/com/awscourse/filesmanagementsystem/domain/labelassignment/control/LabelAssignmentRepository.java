package com.awscourse.filesmanagementsystem.domain.labelassignment.control;

import com.awscourse.filesmanagementsystem.domain.labelassignment.entity.LabelAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LabelAssignmentRepository extends JpaRepository<LabelAssignment, LabelAssignment.Id> {

    List<LabelAssignment> findAllByLabelIdIn(Collection<Long> label);

    List<LabelAssignment> findAllByFileIdIn(Collection<Long> fileIds);

    void deleteAllByLabelIdIn(Collection<Long> labelIds);

    void deleteAllByFileIdAndLabelIdIn(Long fileId, Collection<Long> labelIds);

}
