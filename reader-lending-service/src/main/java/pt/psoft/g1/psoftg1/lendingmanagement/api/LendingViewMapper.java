package pt.psoft.g1.psoftg1.lendingmanagement.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class LendingViewMapper extends MapperInterface {

    @Mapping(target = "lendingNumber", source = "lendingNumber")
    @Mapping(target = "isbn", source = "isbn")
    @Mapping(target = "fineValueInCents", expression = "java(lending.getFineValueInCents().orElse(null))")

    @Mapping(target = "_links.self", source = ".", qualifiedByName = "lendingLink")
    @Mapping(target = "_links.book", source = "isbn", qualifiedByName = "bookLinkByIsbn")
    @Mapping(target = "_links.reader", source = "readerDetails", qualifiedByName = "readerLink")

    @Mapping(target = "returnedDate", source = "returnedDate")
    public abstract LendingView toLendingView(Lending lending);

    public abstract List<LendingView> toLendingView(List<Lending> lendings);

    public abstract LendingsAverageDurationView toLendingsAverageDurationView(Double lendingsAverageDuration);

    // ---------------- Links ----------------

    @Named("lendingLink")
    protected Map<String, String> lendingLink(Lending lending) {
        // Builds: /api/lendings/{year}/{seq} (based on lendingNumber like "2025/12")
        String lendingNumber = lending.getLendingNumber();

        return Map.of("href",
            ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/lendings/")
                .path(lendingNumber)
                .toUriString()
        );
    }

    @Named("bookLinkByIsbn")
    protected Map<String, String> bookLinkByIsbn(String isbn) {
        // Points logically to catalog endpoint. Keep relative for now.
        return Map.of("href",
            ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/books/")
                .path(isbn)
                .toUriString()
        );
    }

    @Named("readerLink")
    protected Map<String, String> readerLink(ReaderDetails readerDetails) {
        // Adjust if your reader endpoint differs.
        // If readerNumber is composite like "2025/3", this works.
        String readerNumber = readerDetails.getReaderNumber().toString();

        return Map.of("href",
            ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/readers/")
                .path(readerNumber)
                .toUriString()
        );
    }
}
