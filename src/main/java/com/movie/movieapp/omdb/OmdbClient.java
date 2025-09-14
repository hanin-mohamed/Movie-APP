package com.movie.movieapp.omdb;

import com.movie.movieapp.common.DTO.OmdbMovieDTO;
import com.movie.movieapp.common.DTO.OmdbSearchResponseDTO;
import com.movie.movieapp.common.exception.customException.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@Slf4j
@RequiredArgsConstructor
public class OmdbClient {

    private static final int MIN_PAGE = 1;
    private static final int MAX_PAGE = 100;
    private static final String OMDB_SEARCH = "OMDb search";
    private static final String OMDB_DETAIL = "OMDb detail";

    private final RestTemplate restTemplate;
    private final OmdbProperties props;

    public OmdbSearchResponseDTO search(String query, int page) {
        if (!StringUtils.hasText(query)){
            log.info("OMDb search called with empty query");
            throw new IllegalArgumentException("Query must not be empty");
        }

        int pageParam = Math.max(MIN_PAGE, Math.min(page, MAX_PAGE));
        URI uri = buildSearchUri(query.trim(), pageParam);

        OmdbSearchResponseDTO res = get(uri, OmdbSearchResponseDTO.class, OMDB_SEARCH);
        if (!res.isOk()) throw new ExternalApiException(res.error() == null ? "OMDb search error" : res.error());
        return res;
    }

    public OmdbMovieDTO getById(String imdbId) {
        if (!StringUtils.hasText(imdbId)) {
            log.warn("OMDb getById called with empty imdbId");
            throw new IllegalArgumentException("imdbId must not be empty");
        }
        URI uri = buildDetailUri(imdbId.trim());

        OmdbMovieDTO res = get(uri, OmdbMovieDTO.class, OMDB_DETAIL);
        if (!res.isOk())
            throw new ExternalApiException(res.error() == null ? "OMDb detail error" : res.error());
        return res;
    }

    private URI buildSearchUri(String query, int page) {
        return UriComponentsBuilder.fromHttpUrl(props.getApiUrl())
                .queryParam("apikey", props.getApiKey())
                .queryParam("s", query)
                .queryParam("page", page)
                .build(true).toUri();
    }

    private URI buildDetailUri(String imdbId) {
        return UriComponentsBuilder.fromHttpUrl(props.getApiUrl())
                .queryParam("apikey", props.getApiKey())
                .queryParam("i", imdbId)
                .queryParam("plot", "full")
                .build(true).toUri();
    }

    private <T> T get(URI uri, Class<T> type, String ctx) {
        try {
            return restTemplate.getForObject(uri, type);
        } catch (RestClientException ex) {
            log.error("{} request to {} failed: {}", ctx, uri, ex.getMessage());
            throw new ExternalApiException(ctx + ": request failed");
        }
    }
}
