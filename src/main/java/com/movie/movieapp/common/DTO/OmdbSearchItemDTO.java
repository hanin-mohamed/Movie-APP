package com.movie.movieapp.common.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OmdbSearchItemDTO(
        @JsonProperty("Title")  String title,
        @JsonProperty("Year")   String year,
        @JsonProperty("imdbID") String imdbId,
        @JsonProperty("Type")   String type,
        @JsonProperty("Poster") String poster)
{

}
