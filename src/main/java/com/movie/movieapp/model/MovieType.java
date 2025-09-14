package com.movie.movieapp.model;

public enum MovieType {
    MOVIE,
    SERIES,
    EPISODE;
    public static MovieType fromOmdb(String value) {
        if (value == null) return MOVIE;
        String v = value.toLowerCase();
        if ("series".equals(v)) return SERIES;
        if ("episode".equals(v)) return EPISODE;
        return MOVIE;
    }
}
