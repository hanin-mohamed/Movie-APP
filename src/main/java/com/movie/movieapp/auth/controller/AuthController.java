package com.movie.movieapp.auth.controller;

import com.movie.movieapp.auth.common.DTO.AuthResponseDTO;
import com.movie.movieapp.auth.common.DTO.LoginRequestDTO;
import com.movie.movieapp.auth.common.DTO.RefreshTokenRequestDTO;
import com.movie.movieapp.auth.service.AuthService;
import com.movie.movieapp.common.response.AppResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("${movies.auth.base-uri}")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication & token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("${movies.auth.login-uri}")
    @Operation( summary = "Login with email & password and get JWT tokens")
    public AppResponse<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return AppResponse.ok("Logged in successfully", authService.login(request));
    }

    @PostMapping("${movies.auth.refresh-token-uri}")
    @Operation(summary = "Refresh tokens using a valid refresh token")
    public AppResponse<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshToken) {
        return AppResponse.ok("Token refreshed", authService.refreshToken(refreshToken));
    }

    @PostMapping("${movies.auth.logout-uri}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Logout by revoking user's active refresh tokens")
    public AppResponse<Void> logout() {
        authService.logout();
        return AppResponse.ok("Logged out Successfully");
    }
}
