package com.movie.movieapp.auth.service;

import com.movie.movieapp.auth.common.DTO.AuthResponseDTO;
import com.movie.movieapp.auth.common.DTO.LoginRequestDTO;
import com.movie.movieapp.auth.common.DTO.RefreshTokenRequestDTO;
import com.movie.movieapp.auth.jwt.JwtService;
import com.movie.movieapp.auth.model.JwtToken;
import com.movie.movieapp.auth.model.TokenType;
import com.movie.movieapp.auth.repository.JwtTokenRepository;
import com.movie.movieapp.entity.MovieUser;
import com.movie.movieapp.repository.MovieUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.jsonwebtoken.ExpiredJwtException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MovieUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtTokenRepository jwtTokenRepository;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        MovieUser user = userRepository.findByEmail(request.email());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return buildTokens(user);
    }

    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String oldRefresh = request.refreshToken();
        String email = jwtService.extractUsernameStrict(oldRefresh);

        MovieUser user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        JwtToken jwtToken = jwtTokenRepository.findByToken(oldRefresh);
        if (jwtToken == null || jwtToken.isExpired() || jwtToken.isRevoked()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        jwtToken.setExpired(true);
        jwtToken.setRevoked(true);
        jwtTokenRepository.save(jwtToken);

        return buildTokens(user);
    }

    @Transactional
    public void logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MovieUser user) {
            revokeAllUserTokens(user);
            return;
        }

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null || attrs.getRequest() == null) return;

        String header = attrs.getRequest().getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return;

        String token = header.substring(7);
        String email;
        try {
            email = jwtService.extractUsername(token);
        } catch (ExpiredJwtException ex) {
            email = ex.getClaims().getSubject();
        } catch (Exception ex) {
            return;
        }

        MovieUser user = userRepository.findByEmail(email);
        if (user != null) {
            revokeAllUserTokens(user);
        }
    }

    private AuthResponseDTO buildTokens(MovieUser user) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("userId", user.getId());
        extra.put("role", user.getRole().name());

        String access = jwtService.generateToken(extra, user);
        String refresh = jwtService.generateRefreshToken(user);
        Date refreshExp = jwtService.extractExpiration(refresh);

        jwtTokenRepository.save(JwtToken.builder()
                .token(refresh)
                .tokenType(TokenType.REFRESH)
                .isExpired(false)
                .isRevoked(false)
                .expiredAt(refreshExp)
                .user(user)
                .build());

        return AuthResponseDTO.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }

    private void revokeAllUserTokens(MovieUser user) {
        List<JwtToken> validTokens = jwtTokenRepository.findAllValidTokenByUser(user.getId());
        if (validTokens.isEmpty()) return;
        for (JwtToken t : validTokens) {
            t.setExpired(true);
            t.setRevoked(true);
        }
        jwtTokenRepository.saveAll(validTokens);
    }
}
