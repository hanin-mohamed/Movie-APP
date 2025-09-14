package com.movie.movieapp.controller;


import com.movie.movieapp.common.DTO.RatingRequestDTO;
import com.movie.movieapp.common.DTO.RatingSummaryDTO;
import com.movie.movieapp.common.response.AppResponse;
import com.movie.movieapp.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${movies.rating.base-uri}")
@RequiredArgsConstructor
public class RatingController {


    private final RatingService ratingService;

    @PutMapping("${movies.rating.rate-movie-uri}")
    public AppResponse<RatingSummaryDTO>rateMovie(@PathVariable String imdbId, @RequestBody @Valid RatingRequestDTO request){
            return AppResponse.ok("Rating saved",  ratingService.rateMovie(imdbId, request));
        }

    @GetMapping("${movies.rating.my-rating-uri}")
    public AppResponse<RatingSummaryDTO> myRating(@PathVariable String imdbId) {
        return AppResponse.ok("Rating fetched", ratingService.getMyRating(imdbId));
    }

    @GetMapping("${movies.rating.rating-summary-uri}")
    public AppResponse<RatingSummaryDTO> summary(@PathVariable String imdbId) {
        return AppResponse.ok("Rating summary", ratingService.getMovieSummary(imdbId));
    }
    @DeleteMapping("${movies.rating.delete-my-rating-uri}")
    public AppResponse<RatingSummaryDTO> deleteMyRating(@PathVariable String imdbId) {
        return AppResponse.ok("Rating removed", ratingService.deleteMyRating(imdbId));
    }
}
