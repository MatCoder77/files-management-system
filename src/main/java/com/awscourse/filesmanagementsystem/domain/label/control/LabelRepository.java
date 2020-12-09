package com.awscourse.filesmanagementsystem.domain.label.control;

import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    List<Label> findAllByCreatedById(Long id);

    List<Label> findAllByNameInAndIdNotIn(Collection<String> names, Collection<Long> ids);

}


