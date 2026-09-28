package com.hr.leave_management.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(
                        ResourceNotFoundException ex, HttpServletRequest request) {
                log.warn("Resource not found: {}", ex.getMessage());
                ErrorResponse error = buildError(
                                HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationErrors(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {
                log.warn("Validation failed for request to {}: {}", request.getRequestURI(), ex.getMessage());
                Map<String, List<String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                                .collect(Collectors.groupingBy(
                                                fe -> fe.getField(),
                                                Collectors.mapping(
                                                                fe -> fe.getDefaultMessage() != null
                                                                                ? fe.getDefaultMessage()
                                                                                : "Invalid value",
                                                                Collectors.toList())));
                ErrorResponse error = buildError(
                                HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), fieldErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(
                        BadRequestException ex, HttpServletRequest request) {
                log.warn("Bad request: {}", ex.getMessage());
                ErrorResponse error = buildError(
                                HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        IllegalArgumentException ex, HttpServletRequest request) {
                log.warn("Illegal argument: {}", ex.getMessage());
                ErrorResponse error = buildError(
                                HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {
                log.error("Unexpected error occurred", ex);
                ErrorResponse error = buildError(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "An unexpected error occurred. Please try again later.",
                                request.getRequestURI(), null);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        // -------------------------------------------------------------------------
        // Private helper
        // -------------------------------------------------------------------------

        private ErrorResponse buildError(HttpStatus httpStatus, String message,
                        String path, Map<String, List<String>> fieldErrors) {
                return ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(httpStatus.value())
                                .error(httpStatus.getReasonPhrase())
                                .message(message)
                                .path(path)
                                .fieldErrors(fieldErrors)
                                .build();
        }
}
