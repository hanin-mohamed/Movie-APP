package com.movie.movieapp.auth.common.DTO;

import lombok.Builder;

@Builder
public record AuthResponseDTO(String accessToken, String refreshToken){}
