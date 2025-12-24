package pt.psoft.g1.psoftg1.genremanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.shared.id.IdGenerator;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final IdGenerator idGenerator;


    @Override
    @org.springframework.cache.annotation.Cacheable(cacheNames = "genreByName", key = "#name")
    public Optional<Genre> findByString(String name) {
        return genreRepository.findByString(name);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(cacheNames = "genreAll")
    public Iterable<Genre> findAll() {
        return genreRepository.findAll();
    }

    @Override
    public List<GenreBookCountDTO> findTopGenreByBooks(){
        Pageable pageableRules = PageRequest.of(0,5);
        return this.genreRepository.findTop5GenreByBookCount(pageableRules).getContent();
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(cacheNames = {"genreAll", "genreByName"}, allEntries = true)
    public Genre save(Genre genre) {
        if (genre == null) throw new IllegalArgumentException("Genre is null");

        // Assign only for new entities
        if (genre.getPk() == null || genre.getPk().isBlank()) {
            genre.assignPk(idGenerator.newId());
        }
        return this.genreRepository.save(genre);
    }

}
