package com.movie.movieapp.common.DTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImportMovieRequestDTO(  @NotEmpty List<@NotBlank String> imdbIds)
{}
