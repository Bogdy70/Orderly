package com.orderly.backend.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiError> handleApiException(ApiException exception) {
    return build(exception.getStatus(), exception.getMessage(), Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> fields = new LinkedHashMap<>();
    exception.getBindingResult().getFieldErrors()
        .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
    return build(HttpStatus.BAD_REQUEST, "Validation failed.", fields);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
    return build(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
  }

  private ResponseEntity<ApiError> build(HttpStatus status, String message, Map<String, String> fields) {
    return ResponseEntity.status(status).body(new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        fields
    ));
  }
}
