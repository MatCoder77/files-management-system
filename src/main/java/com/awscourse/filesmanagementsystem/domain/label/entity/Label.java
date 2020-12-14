package com.awscourse.filesmanagementsystem.domain.label.entity;

import com.awscourse.filesmanagementsystem.domain.auditedobject.AuditedObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.Objects;

import static com.awscourse.filesmanagementsystem.infrastructure.jpa.PersistenceConstants.ID_GENERATOR;

@Entity
@Table(name = "label")
@SQLDelete(sql = "UPDATE label SET object_state = 'REMOVED' WHERE id = ?")
@Loader(namedQuery = "findLabelById")
@NamedQuery(name = "findLabelById", query = "SELECT l FROM Label l WHERE l.id = ?1 AND l.objectState = com.awscourse.filesmanagementsystem.domain.auditedobject.ObjectState.ACTIVE")
@Where(clause = AuditedObject.IS_ACTIVE_OBJECT)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Label extends AuditedObject {

    @Id
    @GeneratedValue(generator = ID_GENERATOR)
    private Long id;

    @NaturalId(mutable = true)
    @NotBlank
    @Size(max = 40)
    @Column(unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private LabelType labelType;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Label)) {
            return false;
        }

        Label label = (Label) o;

        return Objects.equals(getName(), label.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
