package com.example.ddadang.domain.photo.controller;

import com.example.ddadang.domain.photo.dto.PhotoResponse;
import com.example.ddadang.domain.photo.service.PhotoService;
import com.example.ddadang.global.response.ApiResponse;
import com.example.ddadang.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "사진", description = "사진 업로드(MO-MEAL-040, MO-MEMO-010)")
@RestController
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @Operation(
        summary = "사진 업로드",
        description = "multipart/form-data의 file 파트로 사진 1장을 올린다. JPEG/PNG/WEBP/HEIC, 10MB 이하. "
            + "응답의 photoId를 식사/메모 기록 저장 요청의 photoIds에 담으면 기록에 첨부된다."
    )
    @PostMapping(value = "/api/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PhotoResponse>> upload(
        @AuthenticationPrincipal Long memberId,
        @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, photoService.upload(memberId, file));
    }
}
