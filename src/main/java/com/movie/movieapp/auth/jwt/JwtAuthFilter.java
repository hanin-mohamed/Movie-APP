package com.movie.movieapp.auth.jwt;

import com.movie.movieapp.entity.MovieUser;
import com.movie.movieapp.repository.MovieUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MovieUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String jwt = header.substring(7).trim();
        String email;
        try {
            String tokenType = jwtService.extractTokenType(jwt);
            if (!"ACCESS".equals(tokenType)) {
                log.warn("Rejected non-ACCESS token on path={}", request.getRequestURI());
                chain.doFilter(request, response);
                return;
            }
            email = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            chain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            MovieUser movieUser = userRepository.findByEmail(email);
            if (movieUser == null) {
                chain.doFilter(request, response);
                return;
            }
            if (jwtService.isTokenValid(jwt, movieUser)) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(movieUser, null, movieUser.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Authenticated subject={} path={}", email, request.getRequestURI());
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/v3/api-docs") || p.startsWith("/swagger-ui") || p.startsWith("/public/");
    }
}
