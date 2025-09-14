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

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieUserService {

    private static final int  MAX_PAGE_SIZE = 100;
    private static final Sort DEFAULT_SORT  = Sort.by("title").ascending();

    private final MovieRepository movieRepository;
    private final OmdbClient omdbClient;

    public OmdbSearchResponseDTO searchOmdb(String query, int page) {
        return omdbClient.search(query, page);
    }

    @Transactional
    public List<ImportResultDTO> importByImdbIds(ImportMovieRequestDTO request) {

        if (request == null || request.imdbIds() == null || request.imdbIds().isEmpty()) {
            throw new IllegalArgumentException("imdbIds must not be empty");
        }

        List<ImportResultDTO> results = new ArrayList<>();
        Set<String> imported = new HashSet<>();
        for (String raw : request.imdbIds()) {
            results.add(importMovie(raw, imported));
        }
        return results;
    }

    private ImportResultDTO importMovie(String rawImdbId, Set<String> imported) {

        String imdbId = checkRow(rawImdbId);

        if (!imported.add(imdbId)) {
            log.info("Movie with imdbId {} is duplicated in the same request, skipping", imdbId);
            return new ImportResultDTO(imdbId, ImportStatus.EXISTS, "Duplicated in same request");
        }
        if (movieRepository.existsByImdbId(imdbId)) {
            log.info("Movie with imdbId {} already exists, skipping", imdbId);
            return new ImportResultDTO(imdbId, ImportStatus.EXISTS, "Already in database");
        }

        try {
            OmdbMovieDTO detail = omdbClient.getById(imdbId);
            Movie entity = MovieMapper.toEntity(detail);
            movieRepository.save(entity);
            return new ImportResultDTO(imdbId, ImportStatus.ADDED, "Imported successfully");
        } catch (ExternalApiException e) {
            return new ImportResultDTO(imdbId, ImportStatus.FAILED, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return new ImportResultDTO(imdbId, ImportStatus.EXISTS, "Already in database");
        } catch (RuntimeException e) {
            log.error("Unexpected error importing movie with imdbId {}", imdbId, e);
            return new ImportResultDTO(imdbId, ImportStatus.FAILED, "Unexpected error");
        }
    }

    @Transactional(readOnly = true)
    public Page<Movie> listFromDb(String search, int page1Based, int size) {
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
        String id = checkRow(imdbId);
        Movie movie = movieRepository.findByImdbId(id);
        if (movie == null) {
            log.warn("Movie with imdbId {} not found", id);
            throw new NotFoundException("Movie not found");
        }
        return MovieMapper.toDetail(movie);
    }

    @Transactional
    public void deleteMovie(String imdbId) {
        String id = checkRow(imdbId);
        int deleted = movieRepository.deleteByImdbId(id);
        if (deleted == 0) {
            log.warn("Movie with imdbId {} not found for deletion", id);
            throw new NotFoundException("Movie not found");
        }
    }

    @Transactional
    public int deleteByImdbIds(List<String> imdbIds) {
        if (imdbIds == null || imdbIds.isEmpty())
            return 0;
        List<String> ids = new ArrayList<>();
        for (String raw : imdbIds) {
            String id = checkRow(raw);
            ids.add(id);
        }
        return movieRepository.deleteByImdbIdIn(ids);
    }

    private String checkRow(String raw) {
        if (raw == null || raw.trim().isEmpty()){
            log.warn("imdbId is null or empty");
            throw new IllegalArgumentException("imdbId must not be empty");
        }
        return raw.trim();
    }
}
