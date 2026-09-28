package com.example.ddadang.global.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

/**
 * Lombok이 boolean 필드 isSuccess에 대해 생성하는 getter(isSuccess())를 Jackson이
 * "success"라는 별도 프로퍼티로 오인해 중복 직렬화하는 것을 막기 위해, getter 기반 자동 인식을
 * 끄고 필드 기반으로만 직렬화하도록 고정한다.
 */
@Getter
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final boolean isSuccess;
    private final String code;
    private final String message;
    private final T result;

    private ApiResponse(boolean isSuccess, String code, String message, T result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T result) {
        return success(SuccessStatus.OK, result);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(BaseCode code, T result) {
        return ResponseEntity.status(code.getHttpStatus())
            .body(new ApiResponse<>(true, code.getCode(), code.getMessage(), result));
    }

    public static <T> ResponseEntity<ApiResponse<T>> failure(BaseCode code, T result) {
        return ResponseEntity.status(code.getHttpStatus())
            .body(new ApiResponse<>(false, code.getCode(), code.getMessage(), result));
    }

    public static <T> ResponseEntity<ApiResponse<T>> failure(HttpStatusCode httpStatus, String code, String message, T result) {
        return ResponseEntity.status(httpStatus)
            .body(new ApiResponse<>(false, code, message, result));
    }
}
