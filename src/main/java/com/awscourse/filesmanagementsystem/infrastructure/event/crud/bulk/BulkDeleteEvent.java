package com.awscourse.filesmanagementsystem.infrastructure.event.crud.bulk;

import lombok.Getter;

import java.util.List;

@Getter
public class BulkDeleteEvent<T> extends BulkAbstractEvent<T> {

    public BulkDeleteEvent(Object source, List<T> relatedObjects) {
        super(source, relatedObjects);
    }

}