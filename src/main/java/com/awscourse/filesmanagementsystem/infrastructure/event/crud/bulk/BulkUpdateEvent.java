package com.awscourse.filesmanagementsystem.infrastructure.event.crud.bulk;

import java.util.List;

public class BulkUpdateEvent<T> extends BulkAbstractEvent<T> {

    public BulkUpdateEvent(Object source, List<T> relatedObjects) {
        super(source, relatedObjects);
    }

}
