package com.agenticplayer.lifestyle.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.agenticplayer.lifestyle.service.OutdoorSafetyBriefingService;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.AirQualityCheck;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.OutdoorChecklist;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.OutdoorSafetyBriefing;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.WeatherRiskCheck;

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

    @Tool(
            name = "check_air_quality",
            description = """
                    LifeMate(라이프메이트)가 사용자의 지역 또는 좌표를 기준으로 외부 대기질 구조화 데이터를 조회해
                    PM10, PM2.5, US AQI와 마스크·야외활동 주의 여부를 안내합니다.
                    '오늘 미세먼지 어때', '마스크 써야 해', '아이 등원 대기질 괜찮아' 같은 질문에 사용합니다.
                    의료 판단을 대신하지 않으며 민감군은 공식 안내와 전문가 판단을 우선해야 합니다.
                    """)
    public AirQualityCheck checkAirQuality(
            @ToolParam(description = "지역명. 예: 서울 강남구, 인천, 부산 해운대. 좌표를 넣으면 생략 가능", required = false)
            String location,
            @ToolParam(description = "위도. location 대신 정확한 좌표로 조회할 때 사용", required = false)
            Double latitude,
            @ToolParam(description = "경도. location 대신 정확한 좌표로 조회할 때 사용", required = false)
            Double longitude,
            @ToolParam(description = "활동 목적. 예: 아이 등원, 러닝, 산책. 없으면 일반 외출", required = false)
            String activity) {
        return outdoorSafetyService.checkAirQuality(location, latitude, longitude, activity);
    }

    @Tool(
            name = "check_weather_risks",
            description = """
                    LifeMate(라이프메이트)가 외부 날씨 예보 구조화 데이터를 조회해 강수, 자외선, 체감온도,
                    바람 위험을 확인하고 우산·겉옷·자외선 차단 등 필요한 대비를 안내합니다.
                    '우산 챙겨야 해', '자외선 심해', '오늘 자전거 타도 돼' 같은 날씨 위험 질문에 사용합니다.
                    재난·기상특보 판단을 대신하지 않으며 공식 특보와 현지 안내를 우선해야 합니다.
                    """)
    public WeatherRiskCheck checkWeatherRisks(
            @ToolParam(description = "지역명. 예: 제주, 대전, 서울 종로구. 좌표를 넣으면 생략 가능", required = false)
            String location,
            @ToolParam(description = "위도. location 대신 정확한 좌표로 조회할 때 사용", required = false)
            Double latitude,
            @ToolParam(description = "경도. location 대신 정확한 좌표로 조회할 때 사용", required = false)
            Double longitude,
            @ToolParam(description = "활동 목적. 예: 빨래, 자전거, 산책. 없으면 일반 외출", required = false)
            String activity) {
        return outdoorSafetyService.checkWeatherRisks(location, latitude, longitude, activity);
    }

    @Tool(
            name = "recommend_outdoor_checklist",
            description = """
                    LifeMate(라이프메이트)가 지역 또는 좌표와 활동 목적을 바탕으로 외부 날씨·대기질 수치를 조회한 뒤
                    외출 준비물 체크리스트를 생성합니다. 러닝, 아이 등원, 산책, 자전거, 빨래처럼
                    활동별 준비물이 필요한 질문에 사용합니다. 사용자 데이터를 저장하지 않습니다.
                    """)
    public OutdoorChecklist recommendOutdoorChecklist(
            @ToolParam(description = "지역명. 예: 서울 마포구, 부산 해운대, 제주. 좌표를 넣으면 생략 가능", required = false)
            String location,
            @ToolParam(description = "위도. location 대신 정확한 좌표로 조회할 때 사용", required = false)
            Double latitude,
            @ToolParam(description = "경도. location 대신 정확한 좌표로 조회할 때 사용", required = false)
            Double longitude,
            @ToolParam(description = "활동 목적. 예: 러닝, 아이 등원, 빨래, 산책, 자전거", required = false)
            String activity) {
        return outdoorSafetyService.recommendChecklist(location, latitude, longitude, activity);
    }
}
