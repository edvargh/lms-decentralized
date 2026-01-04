package pt.psoft.g1.psoftg1.genremanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;


    public Optional<Genre> findByString(String name) {
        return genreRepository.findByString(name);
    }

    @Override
    public Iterable<Genre> findAll() {
        return genreRepository.findAll();
    }

    @Override
    public List<GenreBookCountDTO> findTopGenreByBooks(){
        Pageable pageableRules = PageRequest.of(0,5);
        return this.genreRepository.findTop5GenreByBookCount(pageableRules).getContent();
    }

    @Override
    public Genre save(Genre genre) {
        return this.genreRepository.save(genre);
    }
}
