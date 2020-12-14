package com.awscourse.filesmanagementsystem.infrastructure.exception;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

    void accept(T arg) throws E;

    static <T, E extends Exception> Consumer<T> wrapper(ThrowingConsumer<T, E> throwingConsumer) {
        return arg -> {
            try {
                throwingConsumer.accept(arg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
