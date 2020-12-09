package com.awscourse.filesmanagementsystem.infrastructure.event.crud.single;

public class UpdateEvent<T> extends AbstractEvent<T> {

    public UpdateEvent(Object source, T affectedObject) {
        super(source, affectedObject);
    }

}
