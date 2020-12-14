package com.awscourse.filesmanagementsystem.infrastructure.exception;

public interface ThrowingRunnable<E extends Exception> {

    void run() throws E;

    static <E extends Exception> Runnable wrapper(ThrowingRunnable throwingRunnable) {
        return () -> {
            try {
                throwingRunnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
