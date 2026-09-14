package com.example.ddadang.domain.cgm.exception;

import com.example.ddadang.domain.cgm.controller.CgmAuthController;
import com.example.ddadang.domain.cgm.controller.CgmController;
import com.example.ddadang.domain.cgm.controller.CgmEventController;
import com.example.ddadang.domain.cgm.status.CgmErrorStatus;
import com.example.ddadang.global.response.ApiResponse;
import com.example.ddadang.global.response.ErrorStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {CgmController.class, CgmAuthController.class, CgmEventController.class})
public class CgmExceptionHandler {

    @ExceptionHandler(IsensApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleIsensApiException(IsensApiException e) {
        return ApiResponse.failure(e.getStatus(), e.getErrorCode(), e.getMessage(), null);
    }

    @ExceptionHandler(InvalidEventTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidEventType(InvalidEventTypeException e) {
        return ApiResponse.failure(CgmErrorStatus.INVALID_EVENT_TYPE, null);
    }

    @ExceptionHandler(InvalidOAuthStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOAuthState(InvalidOAuthStateException e) {
        return ApiResponse.failure(CgmErrorStatus.INVALID_OAUTH_STATE, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        return ApiResponse.failure(ErrorStatus.BAD_REQUEST.getHttpStatus(), ErrorStatus.BAD_REQUEST.getCode(), e.getMessage(), null);
    }
}
