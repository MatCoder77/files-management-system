package com.awscourse.filesmanagementsystem.infrastructure.event.crud.single;

public class CreateEvent<T> extends AbstractEvent<T> {

    public CreateEvent(Object source, T affectedObject) {
        super(source, affectedObject);
    }

}
