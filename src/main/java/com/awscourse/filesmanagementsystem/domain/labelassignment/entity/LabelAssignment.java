package com.awscourse.filesmanagementsystem.domain.labelassignment.entity;

import com.awscourse.filesmanagementsystem.domain.auditedobject.AuditedObject;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "label_assignment")
@SQLDelete(sql = "UPDATE label_assignment SET object_state = 'REMOVED' WHERE id = ?")
@Loader(namedQuery = "findLabelAssignmentById")
@NamedQuery(name = "findLabelAssignmentById", query = "SELECT la FROM LabelAssignment la WHERE la.id = ?1 AND la.objectState = com.awscourse.filesmanagementsystem.domain.auditedobject.ObjectState.ACTIVE")
@Where(clause = AuditedObject.IS_ACTIVE_OBJECT)
@Getter
@NoArgsConstructor
public class LabelAssignment extends AuditedObject {

    @EmbeddedId
    private Id id;

    @MapsId("labelId")
    @ManyToOne
    private Label label;

    @MapsId("fileId")
    @ManyToOne
    private File file;

    public LabelAssignment(Label label, File file) {
        this.id = new Id(label.getId(), file.getId());
        this.label = label;
        this.file = file;
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {

        private Long labelId;
        private Long fileId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id)) return false;

            Id that = (Id) o;

            return Objects.equals(labelId, that.labelId) && Objects.equals(fileId, that.fileId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(labelId, fileId);
        }

    }

}
