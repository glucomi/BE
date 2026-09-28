package com.example.ddadang.domain.glucose.dto.response;

public record CgmSyncResultResponse(
    int fetchedCount,
    int insertedCount,
    int updatedCount
) {
}
