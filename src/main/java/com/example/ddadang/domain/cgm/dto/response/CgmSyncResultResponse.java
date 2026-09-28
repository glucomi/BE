package com.example.ddadang.domain.cgm.dto.response;

public record CgmSyncResultResponse(
    int fetchedCount,
    int insertedCount,
    int updatedCount
) {
}
