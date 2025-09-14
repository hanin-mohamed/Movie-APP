package com.movie.movieapp.common.DTO;

public record MovieDTO(String imdbId, String title, String year, String type, String poster,
        String plot, String genre, String runtime, String director, String actors, String language,
        String country, String awards, String rated, String released
) {}
