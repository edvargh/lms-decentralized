package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderAverageDto;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderLendingsAvgPerMonthDto;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public interface SpringDataLendingRepository extends LendingRepository, LendingRepoCustom, CrudRepository<Lending, Long> {
    @Override
    @Query("SELECT l " +
            "FROM Lending l " +
            "WHERE l.lendingNumber.lendingNumber = :lendingNumber")
    Optional<Lending> findByLendingNumber(String lendingNumber);

    @Query("""
    SELECT l
    FROM Lending l
    WHERE l.readerDetails.readerNumber.readerNumber = :readerNumber
      AND l.isbn = :isbn
""")
    List<Lending> findByReaderNumberAndIsbn(@Param("readerNumber") String readerNumber,
        @Param("isbn") String isbn);


    //http://www.h2database.com/html/commands.html

    @Override
    @Query("SELECT COUNT (l) " +
            "FROM Lending l " +
            "WHERE YEAR(l.startDate) = YEAR(CURRENT_DATE)")
    int getCountFromCurrentYear();

    @Query("""
    SELECT l
    FROM Lending l
    WHERE l.readerDetails.readerNumber.readerNumber = :readerNumber
      AND l.returnedDate IS NULL
""")
    List<Lending> findOutstandingByReaderNumber(@Param("readerNumber") String readerNumber);


    @Override
    @Query(value =
            "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) " +
            "FROM Lending l"
            , nativeQuery = true)
    Double getAverageDuration();

    @Override
    @Query("SELECT AVG(function('DATEDIFF', l.returnedDate, l.startDate)) " +
        "FROM Lending l " +
        "WHERE l.isbn = :isbn AND l.returnedDate IS NOT NULL")
    Double getAvgLendingDurationByIsbn(@Param("isbn") String isbn);
}

interface LendingRepoCustom {
    List<Lending> getOverdue(Page page);
//    List<ReaderAverageDto> getAverageMonthlyPerReader(LocalDate startDate, LocalDate endDate);

}

@RequiredArgsConstructor
class LendingRepoCustomImpl implements LendingRepoCustom {
    // get the underlying JPA Entity Manager via spring thru constructor dependency
    // injection
    private final EntityManager em;

    @Override
    public List<Lending> getOverdue(Page page)
    {

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Lending> cq = cb.createQuery(Lending.class);
        final Root<Lending> root = cq.from(Lending.class);
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();

        // Select overdue lendings where returnedDate is null and limitDate is before the current date
        where.add(cb.isNull(root.get("returnedDate")));
        where.add(cb.lessThan(root.get("limitDate"), LocalDate.now()));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("limitDate"))); // Order by limitDate, oldest first

        final TypedQuery<Lending> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        return q.getResultList();
    }

/*
    @Override
    public List<ReaderAverageDto> getAverageMonthlyPerReader(LocalDate startDate, LocalDate endDate) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        final Root<Lending> lendingRoot = cq.from(Lending.class);
        final Join<ReaderDetails, Lending> readerDetailsJoin = lendingRoot.join("readerDetails");
        final Join<ReaderDetails, User> userJoin = readerDetailsJoin.join("user");

        final List<Predicate> where = new ArrayList<>();

        Expression<Integer> yearExpr = cb.function("YEAR", Integer.class, lendingRoot.get("startDate"));
        Expression<Integer> monthExpr = cb.function("MONTH", Integer.class, lendingRoot.get("startDate"));

        Expression<Double> lendingCount = cb.count(lendingRoot).as(Double.class);

        Expression<Double> diffInMonths = cb.function(
                "date_part", Double.class, cb.literal("month"),
                cb.literal(LocalDate.now())
        );

        Expression<Double> lendingsAveragePerMonth = cb.diff(lendingCount, diffInMonths);



        cq.multiselect(readerDetailsJoin, yearExpr, monthExpr, durationInMonths);

        Expression<Long> durationInMonths = cb.quot(diffInDays, 30L);

        if(startDate!=null)
            where.add(cb.greaterThanOrEqualTo(lendingRoot.get("startDate"), startDate));
        if(endDate!=null)
            where.add(cb.lessThanOrEqualTo(lendingRoot.get("startDate"), endDate));

        cq.where(where.toArray(new Predicate[0]));
        cq.groupBy(readerDetailsJoin.get("readerNumber"), yearExpr, monthExpr);
        cq.orderBy(cb.asc(lendingRoot.get("lendingNumber")));

        List<Tuple> results = em.createQuery(cq).getResultList();
        Map<Integer, Map<Integer, List<ReaderAverageDto>>> groupedResults = new HashMap<>();


        for (Tuple result : results) {
            ReaderDetails readerDetails = result.get(0, ReaderDetails.class);
            int yearValue = result.get(1, Integer.class);
            int monthValue = result.get(2, Integer.class);
            Double averageDurationValue = result.get(3, Double.class);
            ReaderAverageDto readerAverageDto = new ReaderAverageDto(genre, averageDurationValue);

            groupedResults
                    .computeIfAbsent(yearValue, k -> new HashMap<>())
                    .computeIfAbsent(monthValue, k -> new ArrayList<>())
                    .add(readerAverageDto);
        }

        List<ReaderLendingsAvgPerMonthDto> readerLendingsAvgPerMonthDtos = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, List<ReaderAverageDto>>> yearEntry : groupedResults.entrySet()) {
            int yearValue = yearEntry.getKey();
            for (Map.Entry<Integer, List<ReaderAverageDto>> monthEntry : yearEntry.getValue().entrySet()) {
                int monthValue = monthEntry.getKey();
                List<ReaderAverageDto> values = monthEntry.getValue();
                readerLendingsAvgPerMonthDtos.add(new GenreLendingsPerMonthDTO(yearValue, monthValue, values));
            }
        }


        return null;
    }*/
}
