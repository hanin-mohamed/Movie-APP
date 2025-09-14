package com.movie.movieapp.model;

import com.movie.movieapp.common.DTO.MovieDTO;
import com.movie.movieapp.common.DTO.MovieSummaryDTO;
import com.movie.movieapp.common.DTO.OmdbMovieDTO;
import com.movie.movieapp.entity.Movie;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)

public interface MovieMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "type", source = "type", qualifiedByName = "mapType")
    })
    Movie toEntity(OmdbMovieDTO dto);

    MovieSummaryDTO toSummary(Movie movie);

    MovieDTO toDetail(Movie movie);
    @Named("mapType")
    default MovieType mapType(String omdbType) {
        return MovieType.fromOmdb(omdbType);
    }
}
