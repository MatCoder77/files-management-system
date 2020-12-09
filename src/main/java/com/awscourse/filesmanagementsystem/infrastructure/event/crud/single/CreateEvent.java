package com.awscourse.filesmanagementsystem.infrastructure.event.crud.single;

import org.springframework.context.ApplicationEvent;

public class CreateEvent<T> extends ApplicationEvent {

    public CreateEvent(Object source) {
        super(source);
    }

}
