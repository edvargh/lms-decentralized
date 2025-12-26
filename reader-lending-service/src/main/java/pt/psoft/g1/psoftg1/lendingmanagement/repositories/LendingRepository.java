package pt.psoft.g1.psoftg1.lendingmanagement.repositories;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.util.List;
import java.util.Optional;

public interface LendingRepository {
    Optional<Lending> findByLendingNumber(String lendingNumber);
    List<Lending> findByReaderNumberAndIsbn(String readerNumber, String isbn);
    int getCountFromCurrentYear();
    List<Lending> findOutstandingByReaderNumber(String readerNumber);
    Double getAverageDuration();
    Double getAvgLendingDurationByIsbn(String isbn);
    List<Lending> getOverdue(Page page);

    Lending save(Lending lending);

    void delete(Lending lending);

}
