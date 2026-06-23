package com.agenticplayer.lifestyle.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.agenticplayer.lifestyle.tool.LifestyleResponses.LifeEventGuide;

@Service
public class LifeEventGuideService {

    public LifeEventGuide guide(String eventType, String relationship, boolean attending) {
        String event = normalize(eventType);
        String relation = relationship == null || relationship.isBlank() ? "지인" : relationship.trim();

        if (containsAny(event, "부고", "장례", "조문", "상")) {
            return funeralGuide(relation, attending);
        }
        if (containsAny(event, "결혼", "혼인", "웨딩")) {
            return weddingGuide(relation, attending);
        }
        if (containsAny(event, "돌잔치", "첫돌", "돌")) {
            return firstBirthdayGuide(relation, attending);
        }
        return new LifeEventGuide(
                eventType,
                relation,
                List.of("정해진 답은 없습니다. 관계, 참석 여부, 최근에 주고받은 금액과 본인 예산을 기준으로 결정하세요."),
                List.of("초대나 알림에 먼저 감사의 뜻을 전합니다.", "참석 여부를 가능한 한 일찍 알려줍니다."),
                List.of("소식 알려주셔서 감사합니다. 마음을 담아 축하드려요."),
                "지역·세대·모임 문화에 따라 관례가 크게 다르므로 주변의 최근 사례를 함께 확인하세요.");
    }

    private LifeEventGuide weddingGuide(String relationship, boolean attending) {
        boolean close = isClose(relationship);
        List<String> amounts = close
                ? List.of("가까운 사이: 10만 원 이상에서 본인 예산과 기존 왕래를 고려", "매우 가까운 가족·친구: 정액보다 서로의 관례를 우선")
                : List.of("일반 지인·직장 관계: 5만~10만 원을 참고", "식사 참석 시 예식장·모임 관례를 함께 고려");
        return new LifeEventGuide(
                "결혼",
                relationship,
                amounts,
                List.of(
                        attending ? "식 시작 20~30분 전에 도착하면 여유롭습니다." : "불참 연락은 가능한 한 일찍 전합니다.",
                        "축의금 봉투에는 이름과 필요하면 소속을 적습니다.",
                        "금액보다 축하의 뜻과 관계의 지속성이 더 중요합니다."),
                List.of(
                        "결혼 진심으로 축하드려요. 두 분의 앞날에 행복한 일만 가득하길 바랍니다.",
                        "직접 함께하지 못해 아쉽지만, 결혼을 진심으로 축하드립니다."),
                "금액은 법칙이 아니라 참고 범위입니다. 최근에 서로 주고받은 금액과 조직 문화를 우선하세요.");
    }

    private LifeEventGuide funeralGuide(String relationship, boolean attending) {
        boolean close = isClose(relationship);
        List<String> amounts = close
                ? List.of("가까운 사이: 10만 원 이상에서 관계와 본인 형편을 고려", "가족끼리 왕래가 있다면 주변 가족과 금액을 상의")
                : List.of("일반 지인·직장 관계: 5만~10만 원을 참고", "단체로 조의금을 모으는 경우 조직 관례를 우선");
        return new LifeEventGuide(
                "부고·장례",
                relationship,
                amounts,
                List.of(
                        attending ? "검정이나 어두운 계열의 단정한 복장을 권합니다." : "연락은 짧고 차분하게 전합니다.",
                        "유가족에게 고인의 사망 경위를 자세히 묻지 않습니다.",
                        "종교별 조문 방식이 다를 수 있으므로 현장 안내를 따릅니다."),
                List.of(
                        "삼가 고인의 명복을 빕니다. 유가족분들께 깊은 위로의 말씀을 전합니다.",
                        "직접 찾아뵙지 못해 죄송합니다. 멀리서나마 깊은 위로를 전합니다."),
                "장례 예절은 종교와 가족 문화에 따라 다릅니다. 현장 안내와 유가족의 방식을 존중하세요.");
    }

    private LifeEventGuide firstBirthdayGuide(String relationship, boolean attending) {
        return new LifeEventGuide(
                "돌잔치",
                relationship,
                isClose(relationship)
                        ? List.of("가까운 가족·친구: 10만 원 이상 또는 실용적인 선물", "부모가 원하는 육아용품이 있는지 먼저 확인")
                        : List.of("일반 지인: 5만~10만 원 또는 비슷한 예산의 선물"),
                List.of(
                        attending ? "행사 시간과 식사 시작 시간을 미리 확인합니다." : "불참한다면 축하 메시지를 먼저 보냅니다.",
                        "아기 사진을 온라인에 올릴 때는 부모의 동의를 구합니다."),
                List.of("첫돌을 진심으로 축하해요. 건강하고 밝게 자라길 바랍니다."),
                "현금과 선물 중 무엇이 좋은지는 가족마다 다르므로 부모에게 가볍게 물어보는 것이 가장 정확합니다.");
    }

    private boolean isClose(String relationship) {
        String value = normalize(relationship);
        return containsAny(value, "가족", "친한", "절친", "베프", "형제", "자매", "오랜 친구");
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
