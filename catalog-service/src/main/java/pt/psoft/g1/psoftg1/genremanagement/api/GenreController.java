package pt.psoft.g1.psoftg1.genremanagement.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;
import pt.psoft.g1.psoftg1.genremanagement.services.GetAverageLendingsQuery;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;

@Tag(name = "Genres", description = "Endpoints for managing Genres")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {
    private final GenreService genreService;
    private final GenreViewMapper genreViewMapper;

    @PostMapping("/avgLendingsPerGenre")
    public ListResponse<?> getAverageLendings() {
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "This endpoint depends on Lending data and will be implemented in reader-lending-service."
        );
    }

    @GetMapping("/top5")
    public ListResponse<GenreBookCountView> getTop() {
        final var list = genreService.findTopGenreByBooks();

        if(list.isEmpty())
            throw new NotFoundException("No genres to show");

        return new ListResponse<>(genreViewMapper.toGenreBookCountView(list));
    }

    @GetMapping("/lendingsPerMonthLastTwelveMonths")
    public ListResponse<?> getLendingsPerMonthLastYearByGenre() {
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "This endpoint depends on Lending data and will be implemented in reader-lending-service."
        );
    }

    @GetMapping("/lendingsAverageDurationPerMonth")
    public ListResponse<?> getLendingsAverageDurationPerMonth(
        @RequestParam("startDate") String start,
        @RequestParam("endDate") String end
    ) {
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "This endpoint depends on Lending data and will be implemented in reader-lending-service."
        );
    }
}
