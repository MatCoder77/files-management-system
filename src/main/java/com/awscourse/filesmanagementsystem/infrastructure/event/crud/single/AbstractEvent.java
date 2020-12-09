package com.awscourse.filesmanagementsystem.infrastructure.event.crud.single;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AbstractEvent<T> extends ApplicationEvent {

    private final T affectedObject;

    public AbstractEvent(Object source, T affectedObject) {
        super(source);
        this.affectedObject = affectedObject;
    }

}
