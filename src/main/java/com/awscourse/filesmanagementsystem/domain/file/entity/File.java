package com.awscourse.filesmanagementsystem.domain.file.entity;

import com.awscourse.filesmanagementsystem.domain.auditedobject.AuditedObject;
import com.awscourse.filesmanagementsystem.domain.directory.entity.Directory;
import com.awscourse.filesmanagementsystem.domain.labelassignment.entity.LabelAssignment;
import com.awscourse.filesmanagementsystem.domain.labelassignment.entity.LabelAssignment_;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.awscourse.filesmanagementsystem.infrastructure.jpa.PersistenceConstants.ID_GENERATOR;

@Entity
@Table(name = "file")
@SQLDelete(sql = "UPDATE file SET object_state = 'REMOVED' WHERE id = ?")
@Loader(namedQuery = "findFileById")
@NamedQuery(name = "findFileById", query = "SELECT f FROM File f WHERE f.id = ?1 AND f.objectState = com.awscourse.filesmanagementsystem.domain.auditedobject.ObjectState.ACTIVE")
@Where(clause = AuditedObject.IS_ACTIVE_OBJECT)
@Getter
@NoArgsConstructor
public class File extends AuditedObject {

    @Id
    @GeneratedValue(generator = ID_GENERATOR)
    private Long id;

    @NotBlank
    private String name;

    @NaturalId
    @NotBlank
    @Column(length = 1000, unique = true)
    private String resourceIdentifier;

    @NotNull
    private long size;

    @NotNull
    @ManyToOne
    private Directory directory;

    @OneToMany(mappedBy = LabelAssignment_.FILE)
    Set<LabelAssignment> labelAssignments = new HashSet<>();

}
