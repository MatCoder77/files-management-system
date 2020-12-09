package com.awscourse.filesmanagementsystem.domain.directory.entity;

import com.awscourse.filesmanagementsystem.domain.auditedobject.AuditedObject;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

import static com.awscourse.filesmanagementsystem.infrastructure.jpa.PersistenceConstants.ID_GENERATOR;

@Entity
@NoArgsConstructor
public class Directory extends AuditedObject {

    @Id
    @GeneratedValue(generator = ID_GENERATOR)
    private Long id;

    @NotBlank
    private Long name;

    @NaturalId
    @NotBlank
    @Column(unique = true, length = 1000)
    private Long path;

}
