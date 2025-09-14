package com.movie.movieapp.service;

import com.movie.movieapp.common.DTO.RatingRequestDTO;
import com.movie.movieapp.common.DTO.RatingSummaryDTO;

import com.movie.movieapp.common.exception.customException.NotFoundException;
import com.movie.movieapp.entity.Movie;
import com.movie.movieapp.entity.MovieUser;
import com.movie.movieapp.entity.Rating;
import com.movie.movieapp.model.RatingMapper;
import com.movie.movieapp.repository.MovieRepository;
import com.movie.movieapp.repository.MovieUserRepository;
import com.movie.movieapp.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final MovieRepository movieRepository;
    private final MovieUserRepository movieUserRepository;
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;

    @Transactional
    public RatingSummaryDTO rateMovie(String imdbId, RatingRequestDTO request) {
        String id = checkImdbID(imdbId);

        Movie movie = movieRepository.findByImdbId(id);
        if (movie == null) {
            log.warn("Movie not found: {}", id);
            throw new NotFoundException("Movie not found");
        }
        MovieUser user = getCurrentUser();

        Rating rating = ratingRepository.findByUserIdAndMovieImdbId(user.getId(), id);
        if (rating == null) {
            rating = ratingMapper.fromRequest(request);
        } else {
            ratingMapper.updateScore(rating,request);
        }
        ratingRepository.save(rating);

        Double avg = ratingRepository.averageForMovie(id);
        long count = ratingRepository.countForMovie(id);
        return new RatingSummaryDTO(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0, count, rating.getScore());
    }

    @Transactional(readOnly = true)
    public RatingSummaryDTO getMyRating(String imdbId) {
        String id = checkImdbID(imdbId);
        Movie movie = movieRepository.findByImdbId(id);

        if (movie == null)
            throw new NotFoundException("Movie not found");

        MovieUser user = getCurrentUser();
        Rating rating = ratingRepository.findByUserIdAndMovieImdbId(user.getId(), id);

        Double avg = ratingRepository.averageForMovie(id);
        long count = ratingRepository.countForMovie(id);
        Integer myRating = rating == null ? null : rating.getScore();
        return new RatingSummaryDTO(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0, count, myRating);
    }

    @Transactional(readOnly = true)
    public RatingSummaryDTO getMovieSummary(String imdbId) {
        String id = checkImdbID(imdbId);
        Movie movie = movieRepository.findByImdbId(id);
        if (movie == null)
        {
            log.warn("Movie not found: {}", id);
            throw new NotFoundException("Movie not found");
        }

        Double avg = ratingRepository.averageForMovie(id);
        long count = ratingRepository.countForMovie(id);
        return new RatingSummaryDTO(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0, count, null);
    }

    @Transactional
    public RatingSummaryDTO deleteMyRating(String imdbId) {
        String id = checkImdbID(imdbId);
        Movie movie = movieRepository.findByImdbId(id);
        if (movie == null) {
            log.warn("Movie not found: {}", id);
            throw new NotFoundException("Movie not found");
        }

        MovieUser user = getCurrentUser();
        ratingRepository.deleteByUserIdAndMovieImdbId(user.getId(), id);
        Double avg = ratingRepository.averageForMovie(id);
        long count = ratingRepository.countForMovie(id);
        return new RatingSummaryDTO(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0, count, null);
    }

    private MovieUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MovieUser movieUser) {
            return movieUser;
        }
        String email = auth != null ? auth.getName() : null;
        if (email == null) {
            log.warn("Unauthenticated access attempt");
            throw new IllegalStateException("Unauthenticated");
        }
        MovieUser user = movieUserRepository.findByEmail(email);
        if (user == null)
        {
            log.warn("User not found: {}", email);
            throw new NotFoundException("User not found");
        }
        return user;
    }

    private String checkImdbID(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            log.warn("imdbId is null");
            throw new IllegalArgumentException("imdbId must not be empty");
        }
            return raw.trim();
    }
}
