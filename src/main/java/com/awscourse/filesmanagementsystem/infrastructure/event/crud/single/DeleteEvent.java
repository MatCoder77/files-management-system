package com.awscourse.filesmanagementsystem.infrastructure.event.crud.single;

import lombok.Getter;

@Getter
public class DeleteEvent<T> extends AbstractEvent<T> {

    public DeleteEvent(Object source, T affectedObject) {
        super(source, affectedObject);
    }

}
