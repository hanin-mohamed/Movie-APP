package com.movie.movieapp.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class JwtService {

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final Key key;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;

    public JwtService(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.access-exp-seconds}") long accessExpSeconds,
            @Value("${jwt.refresh-exp-seconds}") long refreshExpSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.accessExpSeconds = accessExpSeconds;
        this.refreshExpSeconds = refreshExpSeconds;
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails user) {
        Instant now = Instant.now();
        Map<String, Object> claims = extraClaims == null ? new HashMap<>() : new HashMap<>(extraClaims);
        claims.put(CLAIM_TOKEN_TYPE, TYPE_ACCESS);
        log.debug("Generating ACCESS token for subject={}", user.getUsername());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claim(CLAIM_TOKEN_TYPE, TYPE_REFRESH)
                .setSubject(user.getUsername())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseAll(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return parseAll(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails user) {
        try {
            return extractUsername(token).equals(user.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.warn("Token parsing failed: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    public String extractTokenType(String token) {
        return parseAll(token).get(CLAIM_TOKEN_TYPE, String.class);
    }

    public String extractUsernameStrict(String token) {
        try {
            return extractUsername(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token expired");
            throw new BadCredentialsException("Token expired");
        } catch (JwtException e) {
            log.warn("Invalid token");
            throw new BadCredentialsException("Invalid token");
        }
    }

    private Claims parseAll(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(30)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
