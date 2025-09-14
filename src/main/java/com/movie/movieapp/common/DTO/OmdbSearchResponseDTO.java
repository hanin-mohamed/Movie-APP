package com.movie.movieapp.common.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OmdbSearchResponseDTO(
        @JsonProperty("Search") List<OmdbSearchItemDTO> search,
        @JsonProperty("totalResults") String totalResults,
        @JsonProperty("Response")     String response,
        @JsonProperty("Error")        String error
) {
    public boolean isOk() {
        return response != null && response.equalsIgnoreCase("True");
    }
}