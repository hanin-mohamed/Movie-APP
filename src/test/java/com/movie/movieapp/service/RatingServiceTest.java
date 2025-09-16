package com.movie.movieapp.service;

import com.movie.movieapp.common.DTO.RatingRequestDTO;
import com.movie.movieapp.common.DTO.RatingSummaryDTO;
import com.movie.movieapp.common.exception.customException.NotFoundException;
import com.movie.movieapp.entity.Movie;
import com.movie.movieapp.entity.MovieUser;
import com.movie.movieapp.entity.Rating;
import com.movie.movieapp.model.RatingMapper;
import com.movie.movieapp.repository.MovieRepository;
import com.movie.movieapp.repository.RatingRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private RatingMapper ratingMapper;

    @InjectMocks
    private RatingService service;

    private Movie movie;
    private MovieUser user;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setImdbId("tt123");

        user = new MovieUser();
        user.setId(10L);
        user.setEmail("haneen@gmail.com");

        TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void rateMovie_newRating_createsAndReturnsSummary() {
        when(movieRepository.findByImdbId("tt123")).thenReturn(movie);
        when(ratingRepository.findByUserIdAndMovieImdbId(10L, "tt123")).thenReturn(null);

        RatingRequestDTO request = new RatingRequestDTO(5);
        Rating newRating = new Rating();
        newRating.setScore(5);
        when(ratingMapper.fromRequest(request)).thenReturn(newRating);

        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        when(ratingRepository.averageForMovie("tt123")).thenReturn(4.7);
        when(ratingRepository.countForMovie("tt123")).thenReturn(3L);

        RatingSummaryDTO result = service.rateMovie("tt123", request);

        assertThat(result.average()).isEqualTo(4.7);
        assertThat(result.count()).isEqualTo(3);
        assertThat(result.myRating()).isEqualTo(5);
    }

    @Test
    void rateMovie_movieNotFound_throws() {
        when(movieRepository.findByImdbId("tt404")).thenReturn(null);
        assertThatThrownBy(() -> service.rateMovie("tt404", new RatingRequestDTO(3)))
                .isInstanceOf(NotFoundException.class);
    }
}
