package com.awscourse.filesmanagementsystem.infrastructure.event.crud.bulk;

import com.awscourse.filesmanagementsystem.infrastructure.event.crud.single.AbstractEvent;

public class BulkCreateEvent<T> extends AbstractEvent<T> {

    public BulkCreateEvent(Object source, T affectedObject) {
        super(source, affectedObject);
    }

}
