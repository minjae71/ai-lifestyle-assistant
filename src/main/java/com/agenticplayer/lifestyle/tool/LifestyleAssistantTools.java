package com.agenticplayer.lifestyle.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.agenticplayer.lifestyle.service.OutdoorSafetyBriefingService;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.OutdoorSafetyBriefing;

@Component
public class LifestyleAssistantTools {

    private final OutdoorSafetyBriefingService outdoorSafetyService;

    public LifestyleAssistantTools(OutdoorSafetyBriefingService outdoorSafetyService) {
        this.outdoorSafetyService = outdoorSafetyService;
    }

    @Tool(
            name = "brief_outdoor_safety",
            description = """
                    LifeMate(라이프메이트)가 사용자의 지역과 외출 목적을 바탕으로 외부 구조화 데이터
                    (날씨 예보와 대기질 수치)를 조회해 외출·운동·등원·빨래 같은 일상 활동의 안전 브리핑을 제공합니다.
                    LLM의 일반 지식 답변이 아니라 MCP 도구가 최신 수치 데이터를 조회한 뒤
                    강수, 미세먼지, 자외선, 체감온도, 바람을 종합해 좋음/주의/나쁨/위험 등급과 준비물을 안내합니다.
                    의료·재난 판단을 대신하지 않으며 공식 특보와 현지 안내를 우선해야 합니다.
                    """)
    public OutdoorSafetyBriefing briefOutdoorSafety(
            @ToolParam(description = "지역명. 예: 서울 마포구, 부산 해운대, 제주. 좌표를 넣으면 생략 가능", required = false)
            String location,
            @ToolParam(description = "위도. location 대신 정확한 좌표로 조회할 때 사용", required = false)
            Double latitude,
            @ToolParam(description = "경도. location 대신 정확한 좌표로 조회할 때 사용", required = false)
            Double longitude,
            @ToolParam(description = "외출 목적. 예: 러닝, 아이 등원, 빨래, 산책, 자전거", required = false)
            String activity) {
        return outdoorSafetyService.brief(location, latitude, longitude, activity);
    }
}
