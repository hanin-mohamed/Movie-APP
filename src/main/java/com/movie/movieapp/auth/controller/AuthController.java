package com.movie.movieapp.auth.controller;



import com.movie.movieapp.auth.common.DTO.AuthResponseDTO;
import com.movie.movieapp.auth.common.DTO.LoginRequestDTO;
import com.movie.movieapp.auth.common.DTO.RefreshTokenRequestDTO;
import com.movie.movieapp.auth.service.AuthService;
import com.movie.movieapp.common.response.AppResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${movies.auth.base-uri}")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("${movies.auth.login-uri}")
    public AppResponse<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return AppResponse.ok("Logged in successfully", authService.login(request));
    }

    @PostMapping("${movies.auth.refresh-token-uri}")
    public AppResponse<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshToken) {
        return AppResponse.ok("Token refreshed", authService.refreshToken(refreshToken));
    }

    @PostMapping("${movies.auth.logout-uri}")
    public AppResponse<Void> logout() {
        authService.logout();
        return AppResponse.ok("Logged out Successfully");
    }


}
