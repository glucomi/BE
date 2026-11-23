package com.example.ddadang.domain.photo.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PhotoErrorStatus implements BaseCode {

    EMPTY_FILE(HttpStatus.BAD_REQUEST, "PHOTO_4000", "업로드할 사진이 없습니다."),
    UNSUPPORTED_TYPE(HttpStatus.BAD_REQUEST, "PHOTO_4001", "JPEG, PNG, WEBP, HEIC 형식의 사진만 올릴 수 있습니다."),
    FILE_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "PHOTO_4130", "사진은 10MB 이하만 올릴 수 있습니다."),
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "PHOTO_4040", "사진을 찾을 수 없습니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PHOTO_5000", "사진 업로드에 실패했습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
