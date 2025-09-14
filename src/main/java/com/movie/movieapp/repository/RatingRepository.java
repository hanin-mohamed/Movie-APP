package com.movie.movieapp.repository;

import com.movie.movieapp.entity.Rating;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Rating findByUserIdAndMovieImdbId(Long userId, String imdbId);

    @Query("select avg(r.score) from Rating r where r.movie.imdbId = :imdbId")
    Double averageForMovie(@Param("imdbId") String imdbId);

    @Query("select count(r) from Rating r where r.movie.imdbId = :imdbId")
    long countForMovie(@Param("imdbId") String imdbId);

    void deleteByUserIdAndMovieImdbId(Long userId, String imdbId);
}
