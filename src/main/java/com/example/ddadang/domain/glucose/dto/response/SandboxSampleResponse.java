package com.example.ddadang.domain.glucose.dto.response;

public record SandboxSampleResponse(String message) {

    public static SandboxSampleResponse completed() {
        return new SandboxSampleResponse("샌드박스 테스트 데이터 생성이 완료되었습니다.");
    }
}
