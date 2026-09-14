package com.example.ddadang.domain.cgm.util;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GET /v1/public/cgms는 최대 3개월 범위만 조회 가능하므로,
 * 요청 구간을 3개월 이하의 조각으로 분할한다.
 */
public final class CgmDateRangeSplitter {

    private CgmDateRangeSplitter() {
    }

    public record Range(OffsetDateTime start, OffsetDateTime end) {
    }

    public static List<Range> split(OffsetDateTime start, OffsetDateTime end) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start는 end보다 이전이어야 합니다.");
        }

        List<Range> ranges = new ArrayList<>();
        OffsetDateTime chunkStart = start;
        while (chunkStart.isBefore(end)) {
            OffsetDateTime chunkEnd = chunkStart.plusMonths(3);
            if (chunkEnd.isAfter(end)) {
                chunkEnd = end;
            }
            ranges.add(new Range(chunkStart, chunkEnd));
            chunkStart = chunkEnd;
        }
        return ranges;
    }
}
