package com.movie.movieapp.model;

import com.movie.movieapp.common.DTO.RatingRequestDTO;
import com.movie.movieapp.entity.Rating;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RatingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Rating fromRequest(RatingRequestDTO req);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "score", source = "score")
    void updateScore(@MappingTarget Rating rating, RatingRequestDTO request);
}
