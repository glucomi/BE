package com.example.ddadang.domain.cgm.controller;

import com.example.ddadang.domain.cgm.dto.response.CgmEventResponse;
import com.example.ddadang.domain.cgm.service.CgmEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CGM 이벤트", description = "CareSens Air 앱에 등록한 이벤트(식사/운동/인슐린/케톤/약/메모/자가측정혈당) 조회")
@RestController
@RequiredArgsConstructor
public class CgmEventController {

    private final CgmEventService cgmEventService;

    @Operation(
        summary = "이벤트 데이터 조회",
        description = "i-sens /v1/public/events를 실시간으로 조회한다(자체 DB 미적재). "
            + "eventType은 비워두면 전체 조회, 지정 시 bgm/exercise/insulin/ketone/meal/medicine/memo 중 하나."
    )
    @GetMapping("/api/cgm/events")
    public List<CgmEventResponse> getEvents(
        @RequestParam String isensUserId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @Parameter(description = "bgm/exercise/insulin/ketone/meal/medicine/memo 중 하나, 비워두면 전체 조회")
        @RequestParam(required = false) String eventType
    ) {
        return cgmEventService.getEvents(isensUserId, start, end, eventType);
    }
}
