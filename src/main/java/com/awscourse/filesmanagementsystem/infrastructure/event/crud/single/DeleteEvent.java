package com.awscourse.filesmanagementsystem.infrastructure.event.crud.single;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DeleteEvent<T> extends ApplicationEvent {

    private final T deletedObject;

    public DeleteEvent(Object source, T deletedObject) {
        super(source);
        this.deletedObject = deletedObject;
    }
}
