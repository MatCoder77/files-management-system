package com.awscourse.filesmanagementsystem.infrastructure.exception;

@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {

    void run() throws E;

    static <E extends Exception> Runnable wrapper(ThrowingRunnable<E> throwingRunnable) {
        return () -> {
            try {
                throwingRunnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
