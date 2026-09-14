package com.example.ddadang.domain.cgm.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 * 아이센스 인증/API 서버가 non-2xx 응답을 반환했을 때 던져지는 예외.
 * errorCode는 응답 바디에서 best-effort로 파싱한 값이며, 파싱 실패 시 raw body가 들어간다.
 */
@Getter
public class IsensApiException extends RuntimeException {

    private final HttpStatusCode status;
    private final String errorCode;

    public IsensApiException(HttpStatusCode status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
