package com.shinhan.backend.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleInvalidArgument(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST", "요청 값의 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse("NOT_FOUND", "요청한 주소를 찾을 수 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Unexpected API failure", ex);
        return ResponseEntity.internalServerError().body(new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."));
    }
}
