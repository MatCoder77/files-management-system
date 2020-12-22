package com.awscourse.filesmanagementsystem.domain.file.boundary;

import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.infrastructure.event.crud.bulk.BulkDeleteEvent;

import java.util.List;

public class FileBulkDeletedEvent extends BulkDeleteEvent<File> {

    public FileBulkDeletedEvent(Object source, List<File> relatedObjects) {
        super(source, relatedObjects);
    }

}
