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

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {


    // ------------- Username not found -------------
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UsernameNotFoundException ex,
                                                                        HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.NOT_FOUND.value(),
                "User Not Found",
                "The username you entered does not exist.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ------------- Invalid credentials -------------
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex,
                                                                 HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid Credentials",
                "The username or password you entered is incorrect.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // ------------- JWT expired -------------
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponseDto> handleJwtExpired(ExpiredJwtException ex,
                                                             HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "JWT Token Expired",
                "Your authentication token has expired. Please login again.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // ------------- Malformed or invalid JWT -------------
    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ErrorResponseDto> handleJwtMalformed(Exception ex,
                                                               HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid JWT Token",
                "The provided authentication token is invalid.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // ------------- Validation Errors -------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex,
                                                                   HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed for the request.");

        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ------------- Catch-all Exception -------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception ex,
                                                                HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    // Otp invalid
    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<ErrorResponseDto> handleOtpInvalid(OtpInvalidException ex,
                                                             HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.BAD_REQUEST.value(),
                "Invalid OTP",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Otp expired
    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponseDto> handleOtpExpired(OtpExpiredException ex,
                                                             HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.BAD_REQUEST.value(),
                "OTP expired",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Email sending failed
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailSend(EmailSendException ex,
                                                            HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Email sending failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ------------- Illegal Argument Exception -------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                           HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                false,
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Input",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


}
