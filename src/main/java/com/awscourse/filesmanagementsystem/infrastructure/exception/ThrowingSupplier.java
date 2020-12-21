package com.awscourse.filesmanagementsystem.infrastructure.exception;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {

    T get() throws E;

    static <T, E extends Exception> Supplier<T> wrapper(ThrowingSupplier<T, E> throwingSupplier) {
        return () -> {
            try {
                return throwingSupplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
