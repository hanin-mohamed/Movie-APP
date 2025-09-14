package com.movie.movieapp.service;

import com.movie.movieapp.common.DTO.*;
import com.movie.movieapp.common.exception.customException.ExternalApiException;
import com.movie.movieapp.common.exception.customException.NotFoundException;
import com.movie.movieapp.entity.Movie;
import com.movie.movieapp.model.ImportStatus;
import com.movie.movieapp.model.MovieMapper;
import com.movie.movieapp.omdb.OmdbClient;
import com.movie.movieapp.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieUserService {

    private static final int  MAX_PAGE_SIZE = 100;
    private static final Sort DEFAULT_SORT  = Sort.by("title").ascending();

    private final MovieRepository movieRepository;
    private final OmdbClient omdbClient;
    private final MovieMapper movieMapper;

    public OmdbSearchResponseDTO searchOmdb(String query, int page) {
        return omdbClient.search(query, page);
    }

    @Transactional
    public List<ImportResultDTO> importByImdbIds(ImportMovieRequestDTO request) {
        if (request == null || request.imdbIds() == null || request.imdbIds().isEmpty()) {
            throw new IllegalArgumentException("imdbIds must not be empty");
        }
        List<ImportResultDTO> results = new ArrayList<>(request.imdbIds().size());
        Set<String> imported = new HashSet<>();
        for (String raw : request.imdbIds()) {
            String id = normalizeOrNull(raw);
            if (id == null) {
                results.add(failed(null, "Empty imdbId"));
                continue;
            }
            if (!imported.add(id)) {
                results.add(exists(id, "Duplicated in same request"));
                continue;
            }
            log.info("Movie added to database Successfully");
            results.add(importOneMovie(id));
        }
        return results;
    }

    private ImportResultDTO importOneMovie(String imdbId) {

        if (movieRepository.existsByImdbId(imdbId)) {
            log.info("Movie with imdbId {} already exists, skipping", imdbId);
            return exists(imdbId, "Already in database");
        }

        try {
            OmdbMovieDTO detail = omdbClient.getById(imdbId);
            Movie entity = movieMapper.toEntity(detail);
            movieRepository.save(entity);
            return added(imdbId, "Imported successfully");
        } catch (ExternalApiException e) {
            return failed(imdbId, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return exists(imdbId, "Already in database");
        } catch (RuntimeException e) {
            log.error("Unexpected error importing movie with imdbId {}", imdbId, e);
            return failed(imdbId, "Unexpected error");
        }
    }

    @Transactional(readOnly = true)
    public Page<Movie> getMoviesFromDB(String search, int page1Based, int size) {
        int safeSize  = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int zeroBased = Math.max(page1Based - 1, 0);
        Pageable pageable = PageRequest.of(zeroBased, safeSize, DEFAULT_SORT);

        if (search != null && !search.trim().isEmpty()) {
            return movieRepository.findByTitleContainingIgnoreCase(search.trim(), pageable);
        }
        return movieRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public MovieDTO getDetailByImdbId(String imdbId) {
        String id = requireImdbId(imdbId);
        Movie movie = movieRepository.findByImdbId(id);
        if (movie == null) {
            log.error("Movie with imdbId {} not found", id);
            throw new NotFoundException("Movie not found");
        }
        return movieMapper.toDetail(movie);
    }

    @Transactional
    public void deleteMovie(String imdbId) {
        String id = requireImdbId(imdbId);
        int deleted = movieRepository.deleteByImdbId(id);
        if (deleted == 0) {
            log.error("Movie with imdbId {} not found for deletion", id);
            throw new NotFoundException("Movie not found");
        }
    }

    @Transactional
    public int deleteByImdbIds(List<String> imdbIds) {
        if (imdbIds == null || imdbIds.isEmpty()) return 0;

        List<String> ids = imdbIds.stream()
                .map(this::normalizeOrNull)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (ids.isEmpty()) return 0;
        return movieRepository.deleteByImdbIdIn(ids);
    }

    private String requireImdbId(String raw) {
        if (raw == null) {
            log.error("imdbId is null");
            throw new IllegalArgumentException("imdbId must not be empty");
        }
        String v = raw.trim();
        if (v.isEmpty()) {
            log.error("imdbId is empty");
            throw new IllegalArgumentException("imdbId must not be empty");
        }
        return v;
    }

    private String normalizeOrNull(String raw) {
        if (raw == null) return null;
        String rawValue = raw.trim();
        return rawValue.isEmpty() ? null : rawValue;
    }

    private static ImportResultDTO added(String id, String message) {
        return new ImportResultDTO(id, ImportStatus.ADDED, message);
    }
    private static ImportResultDTO exists(String id, String message) {
        return new ImportResultDTO(id, ImportStatus.EXISTS, message);
    }
    private static ImportResultDTO failed(String id, String message) {
        return new ImportResultDTO(id, ImportStatus.FAILED, message);
    }
}
