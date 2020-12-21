package com.awscourse.filesmanagementsystem.infrastructure.asynchronous;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@UtilityClass
public final class CompletableFutures {

    public static <T> CompletableFuture<List<T>> allOf(Collection<CompletableFuture<T>> futures) {
        return futures.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).thenApply(unused -> list)))
                .thenApply(list -> list.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    public static <T> CompletableFuture<List<T>> allOfOrException(Collection<CompletableFuture<T>> futures) {
        CompletableFuture<List<T>> result = allOf(futures);
        for (CompletableFuture<?> f : futures) {
            f.handle((unused, ex) -> ex == null || result.completeExceptionally(ex));
        }
        return result;
    }

    public static <T> CompletableFuture<T> anyOf(Collection<CompletableFuture<T>> cfs) {
        return CompletableFuture.anyOf(cfs.toArray(new CompletableFuture[0])).thenApply(o -> (T) o);
    }

}