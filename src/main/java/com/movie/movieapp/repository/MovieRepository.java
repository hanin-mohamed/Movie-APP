package com.movie.movieapp.repository;


import com.movie.movieapp.entity.Movie;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Movie findByImdbId(String imdbId);
    boolean existsByImdbId(String imdbId);
    int deleteByImdbId(String imdbId);
    int deleteByImdbIdIn(List<String> imdbIds);
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
