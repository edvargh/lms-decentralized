package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public interface SpringDataReaderRepositoryImpl extends ReaderRepository, ReaderDetailsRepoCustom, CrudRepository<ReaderDetails, Long> {
    @Override
    @Query("SELECT r " +
            "FROM ReaderDetails r " +
            "WHERE r.readerNumber.readerNumber = :readerNumber")
    Optional<ReaderDetails> findByReaderNumber(@Param("readerNumber") @NotNull String readerNumber);

    @Override
    @Query("SELECT r " +
            "FROM ReaderDetails r " +
            "WHERE r.phoneNumber.phoneNumber = :phoneNumber")
    List<ReaderDetails> findByPhoneNumber(@Param("phoneNumber") @NotNull String phoneNumber);

    @Query("SELECT r FROM ReaderDetails r WHERE r.username = :username")
    Optional<ReaderDetails> findByUsername(@Param("username") @NotNull String username);



    @Query(value = "SELECT COUNT(*) FROM READER_DETAILS rd WHERE rd.READER_NUMBER LIKE CONCAT(YEAR(CURDATE()), '/%')", nativeQuery = true)
    int getCountFromCurrentYear();



    @Query("SELECT rd " +
            "FROM ReaderDetails rd " +
            "JOIN Lending l ON l.readerDetails.pk = rd.pk " +
            "GROUP BY rd " +
            "ORDER BY COUNT(l) DESC")
    Page<ReaderDetails> findTopReaders(Pageable pageable);
}

interface ReaderDetailsRepoCustom {

    List<ReaderDetails> searchReaderDetails(pt.psoft.g1.psoftg1.shared.services.Page page, SearchReadersQuery query);
}

@RequiredArgsConstructor
class ReaderDetailsRepoCustomImpl implements ReaderDetailsRepoCustom {

    private final EntityManager em;

    @Override
    public List<ReaderDetails> searchReaderDetails(final pt.psoft.g1.psoftg1.shared.services.Page page, final SearchReadersQuery query) {

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<ReaderDetails> cq = cb.createQuery(ReaderDetails.class);
        final Root<ReaderDetails> readerDetailsRoot = cq.from(ReaderDetails.class);

        cq.select(readerDetailsRoot);

        final List<Predicate> where = new ArrayList<>();

        if (StringUtils.hasText(query.getName())) {
            throw new IllegalArgumentException("Search by name is not supported during migration");
        }

        if (StringUtils.hasText(query.getEmail())) { // exact match
            where.add(cb.equal(readerDetailsRoot.get("username"), query.getEmail()));
            cq.orderBy(cb.asc(readerDetailsRoot.get("username")));
        }

        if (StringUtils.hasText(query.getPhoneNumber())) { // exact match
            where.add(cb.equal(readerDetailsRoot.get("phoneNumber").get("phoneNumber"), query.getPhoneNumber()));
            cq.orderBy(cb.asc(readerDetailsRoot.get("phoneNumber").get("phoneNumber")));
        }


        // search using OR
        if (!where.isEmpty()) {
            cq.where(cb.or(where.toArray(new Predicate[0])));
        }


        final TypedQuery<ReaderDetails> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        return q.getResultList();
    }
}

