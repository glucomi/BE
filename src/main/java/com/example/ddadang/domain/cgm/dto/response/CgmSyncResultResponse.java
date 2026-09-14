package com.example.ddadang.domain.cgm.dto.response;

public record CgmSyncResultResponse(
    int fetchedCount,
    int savedCount
) {
}
