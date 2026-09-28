package com.example.ddadang.domain.cgm.dto.response;

import com.example.ddadang.domain.cgm.entity.CgmReading;
import java.time.OffsetDateTime;

public record CgmReadingResponse(
    String serialNumber,
    Long seqNumber,
    OffsetDateTime eventAt,
    Integer tzOffset,
    Integer stage,
    Double initialValue,
    Double value,
    Double trendRate,
    Integer trend,
    Integer errorCode,
    Integer minMaxFlag
) {

    public static CgmReadingResponse from(CgmReading entity) {
        return new CgmReadingResponse(
            entity.getSerialNumber(),
            entity.getSeqNumber(),
            entity.getEventAt(),
            entity.getTzOffset(),
            entity.getStage(),
            entity.getInitialValue(),
            entity.getValue(),
            entity.getTrendRate(),
            entity.getTrend(),
            entity.getErrorCode(),
            entity.getMinMaxFlag()
        );
    }
}
