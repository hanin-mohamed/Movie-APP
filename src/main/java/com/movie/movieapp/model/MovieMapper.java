package com.movie.movieapp.model;

import com.movie.movieapp.common.DTO.MovieDTO;
import com.movie.movieapp.common.DTO.MovieSummaryDTO;
import com.movie.movieapp.common.DTO.OmdbMovieDTO;
import com.movie.movieapp.entity.Movie;


public class MovieMapper {

    private MovieMapper() {}

    public static Movie toEntity(OmdbMovieDTO d) {
        Movie m = new Movie();
        m.setImdbId(d.imdbId());
        m.setTitle(d.title());
        m.setYear(d.year());
        m.setType(MovieType.fromOmdb(d.type()));
        m.setPoster(d.poster());
        m.setPlot(d.plot());
        m.setGenre(d.genre());
        m.setRuntime(d.runtime());
        m.setDirector(d.director());
        m.setActors(d.actors());
        m.setLanguage(d.language());
        m.setCountry(d.country());
        m.setAwards(d.awards());
        m.setRated(d.rated());
        m.setReleased(d.released());
        return m;
    }

    public static MovieSummaryDTO toSummary(Movie m) {
        return new MovieSummaryDTO(
                m.getImdbId(),
                m.getTitle(),
                m.getYear(),
                m.getType().name(),
                m.getPoster()
        );
    }

    public static MovieDTO toDetail(Movie m) {
        return new MovieDTO(
                m.getImdbId(),
                m.getTitle(),
                m.getYear(),
                m.getType().name(),
                m.getPoster(),
                m.getPlot(),
                m.getGenre(),
                m.getRuntime(),
                m.getDirector(),
                m.getActors(),
                m.getLanguage(),
                m.getCountry(),
                m.getAwards(),
                m.getRated(),
                m.getReleased()
        );
    }
}
