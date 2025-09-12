package com.movie.movieapp.common.exception;

import com.movie.movieapp.common.AppResponse;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AppResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) msg = "Unauthorized";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AppResponse.fail(msg, "AUTH_401"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppResponse<List<Map<String,String>>>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(AppResponse.fail("Validation failed", errors, "VAL_400"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AppResponse<Void>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(AppResponse.fail(ex.getMessage(), "GEN_400"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppResponse<Void>> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AppResponse.fail("Internal server error", "GEN_500"));
    }
}
