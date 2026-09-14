package com.example.ddadang.domain.cgm.exception;

import com.example.ddadang.domain.cgm.controller.CgmAuthController;
import com.example.ddadang.domain.cgm.controller.CgmController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {CgmController.class, CgmAuthController.class})
public class CgmExceptionHandler {

    @ExceptionHandler(IsensApiException.class)
    public ResponseEntity<CgmErrorResponse> handleIsensApiException(IsensApiException e) {
        return ResponseEntity
            .status(e.getStatus())
            .body(new CgmErrorResponse(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CgmErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new CgmErrorResponse("invalid_state", e.getMessage()));
    }

    public record CgmErrorResponse(String errorCode, String message) {
    }
}
