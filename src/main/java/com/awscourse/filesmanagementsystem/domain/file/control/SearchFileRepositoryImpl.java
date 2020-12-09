package com.awscourse.filesmanagementsystem.domain.file.control;

import com.awscourse.filesmanagementsystem.domain.auditedobject.AuditedObject_;
import com.awscourse.filesmanagementsystem.domain.directory.entity.Directory_;
import com.awscourse.filesmanagementsystem.domain.file.boundary.FilesSearchCriteria;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.file.entity.File_;
import com.awscourse.filesmanagementsystem.domain.label.control.LabelRepository;
import com.awscourse.filesmanagementsystem.domain.user.entity.User_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchFileRepositoryImpl implements SearchFileRepository {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private LabelRepository labelRepository;

    private static final Set allowedSoringAttributes = Set.of(File_.NAME, File_.SIZE, File_.CREATED_AT, File_.UPDATED_AT);

    @Override
    public Page<File> searchFilesByCriteria(FilesSearchCriteria searchCriteria, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<File> criteriaQuery = cb.createQuery(File.class);
        Root<File> a = criteriaQuery.from(File.class);
        criteriaQuery.select(a);

        Predicate predicate = createPredicateBasedOnSearchCriteria(a, searchCriteria, cb, criteriaQuery);
        criteriaQuery.where(predicate);

        List<Order> sortingOrders = getSortingOrders(a, pageable.getSort(), cb);
        criteriaQuery.orderBy(sortingOrders);

        TypedQuery<File> query = em.createQuery(criteriaQuery);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, getTotalCount(predicate));
    }

    private Predicate createPredicateBasedOnSearchCriteria(Root<File> root, FilesSearchCriteria searchCriteria,
                                                           CriteriaBuilder cb, CriteriaQuery<File> criteriaQuery) {
        Path<String> name = root.get(File_.name);
        Path<Long> size = root.get(File_.size);
        Path<Long> directory = root.get(File_.directory).get(Directory_.id);
        Path<Instant> createdAt = root.get(AuditedObject_.createdAt);
        Path<Instant> updatedAt = root.get(AuditedObject_.updatedAt);
        Path<String> createdBy = root.get(AuditedObject_.createdBy).get(User_.username);
        Path<String> updatedBy = root.get(AuditedObject_.updatedBy).get(User_.username);
//        Expression<Set<Label>> labels = root.get(File_.labels);

        List<Optional<Predicate>> predicates = Arrays.asList(
                getLikePredicate(name, searchCriteria.getName(), cb),
                getMinMaxPredicate(size, searchCriteria.getMinSize(), searchCriteria.getMaxSize(), cb),
                //getEqualsPredicate(directory, searchCriteria.getDirectory(), cb),
                getMinMaxPredicate(createdAt, searchCriteria.getMinCreatedAt(), searchCriteria.getMaxCreatedAt(), cb),
                getMinMaxPredicate(updatedAt, searchCriteria.getMinLastUpdatedAt(), searchCriteria.getMaxLastUpdatedAt(), cb),
                getEqualsPredicate(createdBy, searchCriteria.getCreatedBy(), cb),
                getEqualsPredicate(updatedBy, searchCriteria.getLastModifiedBy(), cb)
                //getRequiredAttrPredicate(labels, labelRepository.findAllByNameIn(searchCriteria.getLabels()), cb)
        );

        return predicates.stream()
                .flatMap(Optional::stream)
                .collect(Collectors.collectingAndThen(Collectors.toList(), predicatesList -> cb.and(predicatesList.toArray(new Predicate[0]))));
    }

    private Optional<Predicate> getLikePredicate(Path<String> attribute, String value, CriteriaBuilder criteriaBuilder) {
        return Optional.ofNullable(value)
                .map(v -> criteriaBuilder.like(attribute, wrapInWildcards(v)));
    }

    private String wrapInWildcards(String text) {
        return '%' + text + '%';
    }

    private <T extends Comparable<? super T>> Optional<Predicate> getMinMaxPredicate(Path<T> attribute, T min, T max, CriteriaBuilder criteriaBuilder) {
        if (Objects.equals(min, max)) {
            return getEqualsPredicate(attribute, min, criteriaBuilder);
        }

        Optional<Predicate> greaterEqualPredicate = Optional.ofNullable(min)
                .map(val -> criteriaBuilder.greaterThanOrEqualTo(attribute, val));
        Optional<Predicate> lessEqualPredicate = Optional.ofNullable(max)
                .map(val -> criteriaBuilder.greaterThanOrEqualTo(attribute, val));

        List<Predicate> predicates = Arrays.asList(greaterEqualPredicate, lessEqualPredicate).stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (!predicates.isEmpty()) {
            return Optional.ofNullable(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        }
        return Optional.empty();
    }

    private <T> Optional<Predicate> getEqualsPredicate(Path<T> attribute, T value, CriteriaBuilder criteriaBuilder) {
        return Optional.ofNullable(value)
                .map(val -> criteriaBuilder.equal(attribute, val));
    }

    private <T> Optional<Predicate> getRequiredAttrPredicate(Expression<Set<T>> attributes, Collection<T> requiredAttributes, CriteriaBuilder criteriaBuilder) {
        if (requiredAttributes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(requiredAttributes.stream()
                .map(requiredAttribute -> criteriaBuilder.isMember(requiredAttribute, attributes))
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> criteriaBuilder.and(list.toArray(new Predicate[0])))));
    }

    private Long getTotalCount(Predicate predicate) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<File> root = criteriaQuery.from(File.class);
        criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
        return em.createQuery(criteriaQuery).getSingleResult();
    }

    private List<Order> getSortingOrders(Root<File> root, Sort sort, CriteriaBuilder criteriaBuilder) {
        return sort.get()
                .filter(order -> isAllowedSortingAttribute(order.getProperty()))
                .map(order -> mapToCriteriaOrder(root, order, criteriaBuilder))
                .collect(Collectors.toList());
    }

    private boolean isAllowedSortingAttribute(String attributeName) {
        return allowedSoringAttributes.contains(attributeName);
    }

    private Order mapToCriteriaOrder(Root<File> a, Sort.Order order, CriteriaBuilder criteriaBuilder) {
        if (order.isAscending()) {
            return criteriaBuilder.asc(a.get(order.getProperty()));
        }
        return criteriaBuilder.desc(a.get(order.getProperty()));
    }

}
