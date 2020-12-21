package com.awscourse.filesmanagementsystem.infrastructure.transform;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UtilityClass
public class TransformUtils {

    public static <E, E2> List<E2> transformToList(Collection<E> fromCollection, Function<? super E, ? extends E2> elementTransformer) {
        return transform(fromCollection, elementTransformer, ArrayList::new);
    }

    public static <E, E2> Set<E2> transformToSet(Collection<E> fromCollection, Function<? super E, ? extends E2> elementTransformer) {
        return transform(fromCollection, elementTransformer, HashSet::new);
    }

    public static <E, E2, C extends Collection<E2>> C transform(Collection<E> fromCollection,
                                                                Function<? super E, ? extends E2> elementTransformer,
                                                                Supplier<C> collectionFactory) {
        return fromCollection.stream()
                .map(elementTransformer)
                .collect(Collectors.toCollection(collectionFactory));
    }

    public static <E, E2> List<E2> transformToList(Collection<E> fromCollection,
                                                   Predicate<? super E> beforeTransformPredicate,
                                                   Function<? super E, ? extends E2> elementTransformer) {
        return transform(fromCollection, beforeTransformPredicate, elementTransformer, ArrayList::new);
    }

    public static <E, E2> Set<E2> transformToSet(Collection<E> fromCollection,
                                                 Predicate<? super E> beforeTransformPredicate,
                                                 Function<? super E, ? extends E2> elementTransformer) {
        return transform(fromCollection, beforeTransformPredicate, elementTransformer, HashSet::new);
    }

    public static <E, E2, C extends Collection<E2>> C transform(Collection<E> fromCollection,
                                                                Predicate<? super E> beforeTransformPredicate,
                                                                Function<? super E, ? extends E2> elementTransformer,
                                                                Supplier<C> collectionFactory) {
        return fromCollection.stream()
                .filter(beforeTransformPredicate)
                .map(elementTransformer)
                .collect(Collectors.toCollection(collectionFactory));
    }

    public static <E, E2> List<E2> transformToList(Collection<E> fromCollection,
                                                   Function<? super E, ? extends E2> elementTransformer,
                                                   Predicate<? super E2> afterTransformPredicate) {
        return transform(fromCollection, elementTransformer, afterTransformPredicate, ArrayList::new);
    }

    public static <E, E2> Set<E2> transformToSet(Collection<E> fromCollection,
                                                 Function<? super E, ? extends E2> elementTransformer,
                                                 Predicate<? super E2> afterTransformPredicate) {
        return transform(fromCollection, elementTransformer, afterTransformPredicate, HashSet::new);
    }

    public static <E, E2, C extends Collection<E2>> C transform(Collection<E> fromCollection,
                                                                Function<? super E, ? extends E2> elementTransformer,
                                                                Predicate<? super E2> afterTransformPredicate,
                                                                Supplier<C> collectionFactory) {
        return fromCollection.stream()
                .map(elementTransformer)
                .filter(afterTransformPredicate)
                .collect(Collectors.toCollection(collectionFactory));
    }

    public static <E, E2, C extends Collection<E2>> C transform(Collection<E> fromCollection,
                                                                Predicate<? super E> beforeTransformPredicate,
                                                                Function<? super E, ? extends E2> elementTransformer,
                                                                Supplier<C> collectionFactory,
                                                                Predicate<? super E2> afterTransformPredicate) {
        return fromCollection.stream()
                .filter(beforeTransformPredicate)
                .map(elementTransformer)
                .filter(afterTransformPredicate)
                .collect(Collectors.toCollection(collectionFactory));
    }

    public static <E, K, V> Map<K, V> transformToMap(Collection<E> fromCollection,
                                                     Predicate<? super E> beforeTransformPredicate,
                                                     Function<? super E, ? extends K> keyTransformer,
                                                     Function<? super E, ? extends V> valueTransformer) {
        return fromCollection.stream()
                .filter(beforeTransformPredicate)
                .collect(Collectors.toMap(keyTransformer, valueTransformer));
    }

    public static <E, K, V> Map<K, V> transformToMap(Collection<E> fromCollection,
                                                     Function<? super E, ? extends K> keyTransformer,
                                                     Function<? super E, ? extends V> valueTransformer) {
        return fromCollection.stream()
                .collect(Collectors.toMap(keyTransformer, valueTransformer));
    }

    public static <E, T extends Collection<E>> T flattenValues(Map<?, T> map, Supplier<T> collectionFactory) {
        return map.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(collectionFactory));
    }

    public static <K, E, K2, E2, C extends Collection<E2>> Map<K2, C> transformMap(Map<K, ? extends Collection<E>> fromMap,
                                                                                   Function<K, K2> keyTransformer,
                                                                                   Function<E, E2> elementTransformer,
                                                                                   Supplier<C> collectionFactory) {
        return fromMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> keyTransformer.apply(entry.getKey()),
                        entry -> TransformUtils.transform(entry.getValue(), elementTransformer, collectionFactory)));
    }

}
