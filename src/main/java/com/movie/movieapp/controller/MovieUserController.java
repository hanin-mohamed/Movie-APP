package com.movie.movieapp.controller;

import com.movie.movieapp.common.DTO.*;
import com.movie.movieapp.common.response.AppResponse;
import com.movie.movieapp.common.response.PageResponse;
import com.movie.movieapp.entity.Movie;
import com.movie.movieapp.model.MovieMapper;
import com.movie.movieapp.service.MovieUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("${movies.base-uri}")
@Tag(name = "Movies", description = "Admin & user movie operations (DB + OMDb)")
@SecurityRequirement(name = "bearer-jwt")
public class MovieUserController {

    private final MovieUserService movieUserService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("${movies.omdb-search-uri}")
    @Operation(summary = "Admin: search movies on OMDb ")
    public AppResponse<OmdbSearchResponseDTO> searchOmdb(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "1") @Min(1) int page
    ) {
        return AppResponse.ok("OMDb search done", movieUserService.searchOmdb(query, page));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("${movies.import-from-omdb}")
    @Operation(summary = "Admin: import one or multiple movies from OMDb by imdbId")
    public AppResponse<List<ImportResultDTO>> importMovies(@RequestBody @Valid ImportMovieRequestDTO req) {
        return AppResponse.ok("Import finished", movieUserService.importByImdbIds(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("${movies.delete-by-imdbid}")
    @Operation(summary = "Admin: delete a single movie from DB by imdbId")
    public AppResponse<Void> deleteOne(@PathVariable String imdbId) {
        movieUserService.deleteMovie(imdbId);
        return AppResponse.ok("Movie deleted");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("${movies.delete-batch}")
    @Operation(summary = "Admin: batch delete movies by imdbIds")
    public AppResponse<Map<String, Integer>> deleteBatch(@RequestParam("ids") List<String> ids) {
        return AppResponse.ok("Batch delete finished", Map.of("deleted", movieUserService.deleteByImdbIds(ids)));
    }

    @GetMapping("${movies.get-details-by-imdbid}")
    @Operation(summary = "User: get movie details (from DB) by imdbId")
    public AppResponse<MovieDTO> getMovieDetails(@PathVariable String imdbId) {
        return AppResponse.ok("Movie detail", movieUserService.getDetailByImdbId(imdbId));
    }

    @GetMapping
    @Operation(summary = "User: list movies from DB with search & pagination")
    public AppResponse<PageResponse<MovieSummaryDTO>> getMovies(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String search) {
        Page<Movie> p = movieUserService.listFromDb(search, page, size);
        return AppResponse.ok("Movies fetched", PageResponse.of(p, MovieMapper::toSummary));
    }
}
