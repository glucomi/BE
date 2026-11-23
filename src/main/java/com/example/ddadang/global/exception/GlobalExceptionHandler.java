package com.example.ddadang.global.exception;

import com.example.ddadang.domain.photo.status.PhotoErrorStatus;
import com.example.ddadang.global.response.ApiResponse;
import com.example.ddadang.global.response.ErrorStatus;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        return ApiResponse.failure(e.getCode(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return badRequest(message);
    }

    @ExceptionHandler({
        HandlerMethodValidationException.class,
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestPartException.class,
        MultipartException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return badRequest(ErrorStatus.BAD_REQUEST.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ApiResponse.failure(PhotoErrorStatus.FILE_TOO_LARGE, null);
    }

    private ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ApiResponse.failure(
            ErrorStatus.BAD_REQUEST.getHttpStatus(), ErrorStatus.BAD_REQUEST.getCode(), message, null
        );
    }
}
