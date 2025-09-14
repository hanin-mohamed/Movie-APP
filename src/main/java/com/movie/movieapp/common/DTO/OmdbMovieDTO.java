package com.movie.movieapp.common.DTO;


import com.fasterxml.jackson.annotation.JsonProperty;

public record OmdbMovieDTO(
        @JsonProperty("Title")    String title,
        @JsonProperty("Year")     String year,
        @JsonProperty("imdbID")   String imdbId,
        @JsonProperty("Type")     String type,
        @JsonProperty("Poster")   String poster,
        @JsonProperty("Plot")     String plot,
        @JsonProperty("Genre")    String genre,
        @JsonProperty("Runtime")  String runtime,
        @JsonProperty("Director") String director,
        @JsonProperty("Actors")   String actors,
        @JsonProperty("Language") String language,
        @JsonProperty("Country")  String country,
        @JsonProperty("Awards")   String awards,
        @JsonProperty("Rated")    String rated,
        @JsonProperty("Released") String released,
        @JsonProperty("Response") String response,
        @JsonProperty("Error")    String error
) {
    public boolean isOk() {
        return response != null && response.equalsIgnoreCase("True");
    }
}
