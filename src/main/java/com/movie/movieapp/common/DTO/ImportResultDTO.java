package com.movie.movieapp.common.DTO;




import com.movie.movieapp.model.ImportStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Setter
@Getter
public  class ImportResultDTO {
    @Getter
    private final String imdbId;
    private final ImportStatus status;
    private final String message;
}