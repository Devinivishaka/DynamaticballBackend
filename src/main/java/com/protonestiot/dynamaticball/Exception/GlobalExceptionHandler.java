package com.protonestiot.dynamaticball.Exception;

import com.protonestiot.dynamaticball.Dto.ErrorResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Common method to build error response
    private ResponseEntity<ErrorResponseDto> buildError(String error, String message, HttpStatus status, HttpServletRequest request) {
        ErrorResponseDto body = new ErrorResponseDto(false, status.value(), error, message, request.getRequestURI());
        return new ResponseEntity<>(body, status);
    }

    // ---------------- Login/User exceptions ----------------
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        return buildError("User Not Found", ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildError("Invalid Credentials", ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
    }

    // ---------------- JWT exceptions ----------------
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponseDto> handleJwtExpired(ExpiredJwtException ex, HttpServletRequest request) {
        return buildError("JWT Token Expired", "Your authentication token has expired. Please login again.", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ErrorResponseDto> handleJwtMalformed(Exception ex, HttpServletRequest request) {
        return buildError("Invalid JWT Token", "The provided authentication token is invalid.", HttpStatus.UNAUTHORIZED, request);
    }

    // ---------------- Validation errors ----------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream().map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("Validation failed for the request.");
        return buildError("Validation Error", message, HttpStatus.BAD_REQUEST, request);
    }

    // ---------------- Other common exceptions ----------------
    @ExceptionHandler({IllegalArgumentException.class, RuntimeException.class})
    public ResponseEntity<ErrorResponseDto> handleCommonExceptions(Exception ex, HttpServletRequest request) {
        return buildError("Error", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    // ---------------- Catch-all ----------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAll(Exception ex, HttpServletRequest request) {
        return buildError("Internal Server Error", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
