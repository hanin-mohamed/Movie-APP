package com.movie.movieapp.service;

import com.movie.movieapp.common.DTO.ImportMovieRequestDTO;
import com.movie.movieapp.common.DTO.ImportResultDTO;
import com.movie.movieapp.common.DTO.OmdbMovieDTO;
import com.movie.movieapp.common.exception.customException.NotFoundException;
import com.movie.movieapp.entity.Movie;
import com.movie.movieapp.model.ImportStatus;
import com.movie.movieapp.model.MovieMapper;
import com.movie.movieapp.omdb.OmdbClient;
import com.movie.movieapp.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieUserServiceTest {

    @Mock private MovieRepository movieRepository;
    @Mock private OmdbClient omdbClient;
    @Mock private MovieMapper movieMapper;

    @InjectMocks private MovieUserService service;

    @BeforeEach
    void setup() { }


    // importByImdbIds tests - successful addition
    @Test
    void importByImdbIds_addedSuccessfully() {
        ImportMovieRequestDTO req = new ImportMovieRequestDTO(List.of("tt1234567"));

        when(movieRepository.existsByImdbId("tt1234567")).thenReturn(false);
        OmdbMovieDTO omdbDto = new OmdbMovieDTO(
                "tt1234567", "Test Movie", "2024",
                "movie", "http://poster", "Plot...", "Action", "120 min",
                "Someone", "Actor1, Actor2", "English", "USA", "Awards", "PG-13", "2024-01-01", "ok", null
        );
        when(omdbClient.getById("tt1234567")).thenReturn(omdbDto);

        Movie entity = new Movie();
        entity.setImdbId("tt1234567");
        entity.setTitle("Test Movie");
        when(movieMapper.toEntity(omdbDto)).thenReturn(entity);

        List<ImportResultDTO> result = service.importByImdbIds(req);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ImportStatus.ADDED);
        verify(movieRepository).save(entity);
    }


    //  duplicate imdbId in same request
    @Test
    void importByImdbIds_duplicatesInSameRequest_flaggedExists() {
        ImportMovieRequestDTO req = new ImportMovieRequestDTO(List.of("tt1", "tt1"));
        List<ImportResultDTO> result = service.importByImdbIds(req);
        assertThat(result).hasSize(2);
        assertThat(result.get(1).getStatus()).isEqualTo(ImportStatus.EXISTS);
        assertThat(result.get(1).getMessage()).contains("Duplicated");
    }


    // movie not found on OMDb
    @Test
    void getDetailByImdbId_notFound_throws() {
        when(movieRepository.findByImdbId("tt404")).thenReturn(null);
        assertThatThrownBy(() -> service.getDetailByImdbId("tt404"))
                .isInstanceOf(NotFoundException.class);
    }
    @Test
    void getMoviesFromDB_withSearch_callsSearchRepo() {
        PageRequest.of(0, 15, Sort.by("title").ascending()); // expected
        when(movieRepository.findByTitleContainingIgnoreCase(eq("bat"), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page p = service.getMoviesFromDB("bat", 1, 15);

        assertThat(p.getContent()).isEmpty();
        verify(movieRepository).findByTitleContainingIgnoreCase(eq("bat"), any(Pageable.class));
    }
}
