package pt.psoft.g1.psoftg1.genremanagement.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;

@Tag(name = "Genres", description = "Endpoints for managing Genres")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {
    private final GenreService genreService;
    private final GenreViewMapper genreViewMapper;

    @GetMapping("/top5")
    public ListResponse<GenreBookCountView> getTop() {
        final var list = genreService.findTopGenreByBooks();

        if(list.isEmpty())
            throw new NotFoundException("No genres to show");

        return new ListResponse<>(genreViewMapper.toGenreBookCountView(list));
    }

}
