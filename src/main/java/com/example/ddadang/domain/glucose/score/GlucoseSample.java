package com.example.ddadang.domain.glucose.score;

import java.time.OffsetDateTime;

public record GlucoseSample(OffsetDateTime time, double value) {
}
