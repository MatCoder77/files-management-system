package com.awscourse.filesmanagementsystem.domain.label.boundary;

import com.awscourse.filesmanagementsystem.domain.label.entity.Label;
import com.awscourse.filesmanagementsystem.infrastructure.event.crud.bulk.BulkDeleteEvent;

import java.util.List;

public class LabelBulkDeletedEvent extends BulkDeleteEvent<Label> {

    public LabelBulkDeletedEvent(Object source, List<Label> relatedObjects) {
        super(source, relatedObjects);
    }

}
