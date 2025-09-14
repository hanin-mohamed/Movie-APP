package com.movie.movieapp.entity;

import com.movie.movieapp.model.MovieType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 16)
    private String imdbId;

    @NotBlank
    @Column(nullable = false, length = 512)
    private String title;

    @Column(length = 8)
    private String year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MovieType type;

    @Column(length = 1024)
    private String poster;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String plot;

    @Column(length = 512)
    private String genre;

    @Column(length = 64)
    private String runtime;

    @Column(length = 512)
    private String director;

    @Column(length = 1024)
    private String actors;

    @Column(length = 128)
    private String language;

    @Column(length = 128)
    private String country;

    @Column(length = 512)
    private String awards;

    @Column(length = 32)
    private String rated;

    @Column(length = 32)
    private String released;
}
