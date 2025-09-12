package com.movie.movieapp.auth.common.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record LoginRequestDTO (@Email @NotBlank(message = "Email is required") String email, @NotBlank(message = "Password is required") String password) {}
