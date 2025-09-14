package com.movie.movieapp.omdb;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "omdb")
public class OmdbProperties {
    private String apiUrl;
    private String apiKey;
}