package com.awscourse.filesmanagementsystem.domain.file.control;

import com.awscourse.filesmanagementsystem.domain.auditedobject.AuditedObject_;
import com.awscourse.filesmanagementsystem.domain.file.boundary.FilesSearchCriteria;
import com.awscourse.filesmanagementsystem.domain.file.entity.File;
import com.awscourse.filesmanagementsystem.domain.file.entity.File_;
import com.awscourse.filesmanagementsystem.domain.label.entity.Label_;
import com.awscourse.filesmanagementsystem.domain.labelassignment.entity.LabelAssignment;
import com.awscourse.filesmanagementsystem.domain.labelassignment.entity.LabelAssignment_;
import com.awscourse.filesmanagementsystem.domain.user.entity.User_;
import com.awscourse.filesmanagementsystem.infrastructure.jpa.LikeClauseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchFileRepositoryImpl implements SearchFileRepository {

    private static final Set<String> allowedSoringAttributes =
            Set.of(File_.NAME, File_.SIZE, AuditedObject_.CREATED_AT, AuditedObject_.UPDATED_AT);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<File> searchFilesByCriteria(FilesSearchCriteria searchCriteria, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<File> criteriaQuery = cb.createQuery(File.class);
        Root<File> root = criteriaQuery.from(File.class);
        criteriaQuery.select(root);

        Predicate predicate = createPredicateBasedOnSearchCriteria(root, searchCriteria, cb, criteriaQuery);
        criteriaQuery.where(predicate);

        List<Order> sortingOrders = getSortingOrders(root, pageable.getSort(), cb);
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
        Path<String> path = root.get(File_.path);
        Path<Instant> createdAt = root.get(AuditedObject_.createdAt);
        Path<Instant> updatedAt = root.get(AuditedObject_.updatedAt);
        Path<String> createdBy = root.get(AuditedObject_.createdBy).get(User_.username);
        Path<String> updatedBy = root.get(AuditedObject_.updatedBy).get(User_.username);
        List<Optional<Predicate>> predicates = Arrays.asList(
                getLikePredicate(name, searchCriteria.getName(), cb),
                getMinMaxPredicate(size, searchCriteria.getMinSize(), searchCriteria.getMaxSize(), cb),
                getStartsWithPredicate(path, searchCriteria.getPath(), cb),
                getMinMaxPredicate(createdAt, searchCriteria.getMinCreatedAt(), searchCriteria.getMaxCreatedAt(), cb),
                getMinMaxPredicate(updatedAt, searchCriteria.getMinLastUpdatedAt(), searchCriteria.getMaxLastUpdatedAt(), cb),
                getEqualsPredicate(createdBy, searchCriteria.getCreatedBy(), cb),
                getEqualsPredicate(updatedBy, searchCriteria.getLastModifiedBy(), cb),
                getContainsAnyOfLabelsPredicate(root, criteriaQuery, searchCriteria, cb),
                getContainsAllOfLabelsPredicate(root, criteriaQuery, searchCriteria, cb));
        return predicates.stream()
                .flatMap(Optional::stream)
                .collect(Collectors.collectingAndThen(Collectors.toList(), predicatesList -> cb.and(predicatesList.toArray(new Predicate[0]))));
    }

    private Optional<Predicate> getLikePredicate(Path<String> attribute, String value, CriteriaBuilder criteriaBuilder) {
        return Optional.ofNullable(value)
                .map(v -> criteriaBuilder.like(attribute, LikeClauseUtils.getWrappedInContainsPattern(v), LikeClauseUtils.ESCAPE_CHARACTER));
    }

    private Optional<Predicate> getStartsWithPredicate(Path<String> attribute, String value, CriteriaBuilder criteriaBuilder) {
        return Optional.ofNullable(value)
                .map(v -> criteriaBuilder.like(attribute, LikeClauseUtils.getWrappedInStartsWithPattern(v), LikeClauseUtils.ESCAPE_CHARACTER));
    }

    private <T extends Comparable<? super T>> Optional<Predicate> getMinMaxPredicate(Path<T> attribute, T min, T max, CriteriaBuilder criteriaBuilder) {
        if (Objects.equals(min, max)) {
            return getEqualsPredicate(attribute, min, criteriaBuilder);
        }
        Optional<Predicate> greaterEqualPredicate = Optional.ofNullable(min)
                .map(val -> criteriaBuilder.greaterThanOrEqualTo(attribute, val));
        Optional<Predicate> lessEqualPredicate = Optional.ofNullable(max)
                .map(val -> criteriaBuilder.greaterThanOrEqualTo(attribute, val));
        List<Predicate> predicates = Stream.of(greaterEqualPredicate, lessEqualPredicate)
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

    private Optional<Predicate> getContainsAnyOfLabelsPredicate(Root<File> root, CriteriaQuery<File> criteriaQuery, FilesSearchCriteria searchCriteria, CriteriaBuilder criteriaBuilder) {
        List<String> allowedLabels = searchCriteria.getContainsAnyOfLabels();
        if (labelsAreNotSpecified(allowedLabels)) {
            return Optional.empty();
        }
        return Optional.ofNullable(criteriaBuilder.exists(getLabelIdsSubquery(allowedLabels, root, criteriaQuery, criteriaBuilder)));
    }

    private Subquery<Long> getLabelIdsSubquery(Collection<String> inClauseLabelNames,
                                               Root<File> parentQueryRoot,
                                               CriteriaQuery<File> criteriaQuery,
                                               CriteriaBuilder criteriaBuilder) {
        Subquery<Long> labelAssignmentSubquery = criteriaQuery.subquery(Long.class);
        Root<LabelAssignment> subqueryRoot = labelAssignmentSubquery.from(LabelAssignment.class);
        Root<File> correlatedParentQueryRoot = labelAssignmentSubquery.correlate(parentQueryRoot);
        Path<Long> labelId = subqueryRoot.get(LabelAssignment_.label).get(Label_.id);
        Path<String> labelName = subqueryRoot.get(LabelAssignment_.label).get(Label_.name);
        Path<Long> fileId = subqueryRoot.get(LabelAssignment_.file).get(File_.id);
        Path<Long> correlatedRootFileId = correlatedParentQueryRoot.get(File_.id);
        return labelAssignmentSubquery.select(labelId)
                .where(criteriaBuilder.equal(fileId, correlatedRootFileId), labelName.in(inClauseLabelNames));
    }

    private boolean labelsAreNotSpecified(Collection<String> labels) {
        return Optional.ofNullable(labels)
                .filter(v -> !v.isEmpty())
                .isEmpty();
    }

    private Optional<Predicate> getContainsAllOfLabelsPredicate(Root<File> root, CriteriaQuery<File> criteriaQuery, FilesSearchCriteria searchCriteria, CriteriaBuilder criteriaBuilder) {
        List<String> labels = searchCriteria.getContainsAllOfLabels();
        if (labelsAreNotSpecified(labels)) {
            return Optional.empty();
        }
        return Optional.ofNullable(criteriaBuilder.greaterThanOrEqualTo(getCountLabelIdsSubquery(labels, root, criteriaQuery, criteriaBuilder), (long) labels.size()));
    }

    private Subquery<Long> getCountLabelIdsSubquery(Collection<String> inClauseLabelNames,
                                               Root<File> parentQueryRoot,
                                               CriteriaQuery<File> criteriaQuery,
                                               CriteriaBuilder criteriaBuilder) {
        Subquery<Long> labelAssignmentSubquery = criteriaQuery.subquery(Long.class);
        Root<LabelAssignment> subqueryRoot = labelAssignmentSubquery.from(LabelAssignment.class);
        Root<File> correlatedParentQueryRoot = labelAssignmentSubquery.correlate(parentQueryRoot);
        Path<Long> labelId = subqueryRoot.get(LabelAssignment_.label).get(Label_.id);
        Path<String> labelName = subqueryRoot.get(LabelAssignment_.label).get(Label_.name);
        Path<Long> fileId = subqueryRoot.get(LabelAssignment_.file).get(File_.id);
        Path<Long> correlatedRootFileId = correlatedParentQueryRoot.get(File_.id);
        return labelAssignmentSubquery.select(criteriaBuilder.count(labelId))
                .where(criteriaBuilder.equal(fileId, correlatedRootFileId), labelName.in(inClauseLabelNames));
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
