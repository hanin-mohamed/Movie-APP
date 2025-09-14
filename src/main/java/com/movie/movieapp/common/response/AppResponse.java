package com.movie.movieapp.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppResponse<T>(
        boolean success,
        String message,
        T data,
        String path,
        Instant timestamp,
        String code
) {
    private static String currentPath() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs.getRequest().getRequestURI();
    }

    public static <T> AppResponse<T> ok(String message, T data) {
        return new AppResponse<>(true, message, data, currentPath(), Instant.now(), null);
    }
    public static AppResponse<Void> ok(String message) {
        return new AppResponse<>(true, message, null, currentPath(), Instant.now(), null);
    }

    public static AppResponse<Void> fail(String message, String code) {
        return new AppResponse<>(false, message, null, currentPath(), Instant.now(), code);
    }
    public static <T> AppResponse<T> fail(String message, T data, String code) {
        return new AppResponse<>(false, message, data, currentPath(), Instant.now(), code);
    }
}
