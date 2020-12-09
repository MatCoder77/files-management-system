package com.awscourse.filesmanagementsystem.infrastructure.event.crud.bulk;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class BulkAbstractEvent<T> extends ApplicationEvent {

    private final List<T> relatedObjects;

    public BulkAbstractEvent(Object source, List<T> relatedObjects) {
        super(source);
        this.relatedObjects = relatedObjects;
    }

}
