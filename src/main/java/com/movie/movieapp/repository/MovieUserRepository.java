package com.movie.movieapp.repository;

import com.movie.movieapp.entity.MovieUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieUserRepository extends JpaRepository<MovieUser, Long> {
    MovieUser findByEmail(String email);

}

