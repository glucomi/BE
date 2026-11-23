package com.example.ddadang.domain.photo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PhotoResponse(
    Long photoId,
    @Schema(description = "사진 조회 URL(presigned URL, 1시간 후 만료). 만료되면 기록을 다시 조회해 새 URL을 받는다.") String url
) {
}
