package com.movie.movieapp.auth.repository;

import com.movie.movieapp.auth.model.JwtToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    JwtToken findByToken(String token);

    @Query("""
           select t from JwtToken t
           where t.user.id = :userId 
             and t.isRevoked = false 
             and t.isExpired = false
           """)
    List<JwtToken> findAllValidTokenByUser(@Param("userId") Long userId);
}
