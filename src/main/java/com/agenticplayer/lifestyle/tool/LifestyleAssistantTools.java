package com.agenticplayer.lifestyle.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.agenticplayer.lifestyle.service.ExpenseSettlementService;
import com.agenticplayer.lifestyle.service.LifeEventGuideService;
import com.agenticplayer.lifestyle.service.MealSuggestionService;
import com.agenticplayer.lifestyle.service.MessageDraftService;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.LifeEventGuide;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.MealResult;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.MessageDraftResult;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.SettlementResult;

@Component
public class LifestyleAssistantTools {

    private final MealSuggestionService mealService;
    private final ExpenseSettlementService settlementService;
    private final LifeEventGuideService lifeEventService;
    private final MessageDraftService messageService;

    public LifestyleAssistantTools(
            MealSuggestionService mealService,
            ExpenseSettlementService settlementService,
            LifeEventGuideService lifeEventService,
            MessageDraftService messageService) {
        this.mealService = mealService;
        this.settlementService = settlementService;
        this.lifeEventService = lifeEventService;
        this.messageService = messageService;
    }

    @Tool(
            name = "suggest_meals_from_ingredients",
            description = """
                    LifeMate(라이프메이트)가 사용자가 보유한 식재료와 조리 가능 시간을 바탕으로
                    만들기 쉬운 메뉴를 최대 3개 추천합니다. '오늘 뭐 먹지', '계란과 김치로 만들 음식',
                    '10분 안에 가능한 요리' 같은 요청에 사용합니다. 알레르기나 의학적 식단 판단에는 사용하지 않습니다.
                    """)
    public MealResult suggestMeals(
            @ToolParam(description = "보유 재료. 쉼표 또는 줄바꿈으로 구분. 예: 계란, 밥, 김치", required = true)
            String ingredients,
            @ToolParam(description = "허용 조리시간(분). 생략하면 30분", required = false)
            Integer maxMinutes,
            @ToolParam(description = "선호. 예: 매운맛, 담백한, 국물. 없으면 생략", required = false)
            String preference) {
        int effectiveMaxMinutes = maxMinutes == null ? 30 : maxMinutes;
        return mealService.suggest(ingredients, effectiveMaxMinutes, preference);
    }

    @Tool(
            name = "split_group_expenses",
            description = """
                    LifeMate(라이프메이트)가 대화 형식으로 적힌 모임 지출을 동일 비율로 정산하고,
                    누가 누구에게 얼마를 보내야 하는지 간결한 송금 목록을 만듭니다.
                    '민수 20000, 영희 30000' 같은 모임 비용 정산 요청에 사용합니다.
                    """)
    public SettlementResult splitExpenses(
            @ToolParam(description = "전체 참여자 이름. 쉼표 또는 공백으로 구분. 예: 민수, 영희, 철수", required = true)
            String participants,
            @ToolParam(description = "결제자와 금액을 한 줄씩 입력. 예: 민수 20000\\n영희 30000", required = true)
            String expenses) {
        return settlementService.settle(participants, expenses);
    }

    @Tool(
            name = "guide_korean_life_event",
            description = """
                    LifeMate(라이프메이트)가 결혼식, 부고·장례, 돌잔치 등 한국 경조사의
                    금액 선택 기준, 기본 예절과 보낼 문구를 안내합니다. 금액은 정답이 아닌 참고 범위로 제공합니다.
                    축의금, 부의금, 조문 예절 또는 축하·위로 문구 질문에 사용합니다.
                    """)
    public LifeEventGuide guideLifeEvent(
            @ToolParam(description = "행사 종류. 예: 결혼식, 부고, 돌잔치", required = true)
            String eventType,
            @ToolParam(description = "상대와의 관계. 예: 회사 대리님, 친한 친구, 친척", required = true)
            String relationship,
            @ToolParam(description = "직접 참석하면 true, 불참하거나 모르면 생략", required = false)
            Boolean attending) {
        return lifeEventService.guide(eventType, relationship, Boolean.TRUE.equals(attending));
    }

    @Tool(
            name = "draft_message",
            description = """
                    LifeMate(라이프메이트)가 회사, 가족, 친구 관계에서 보내기 어려운 메시지 초안을 작성합니다.
                    연차 요청, 일정 변경, 정중한 거절, 사과처럼 말투 조절이 필요한 요청에 사용합니다.
                    사용자가 보내기 전에 이름, 날짜와 사실관계를 직접 확인해야 합니다.
                    """)
    public MessageDraftResult draftMessage(
            @ToolParam(description = "메시지를 보내는 상황. 예: 연차 재요청, 약속 변경, 정중한 거절", required = true)
            String situation,
            @ToolParam(description = "받는 사람 또는 관계. 예: 팀장님, 친구", required = true)
            String recipient,
            @ToolParam(description = "원하는 말투. 예: 정중한, 친근한, 간결한. 없으면 생략", required = false)
            String tone,
            @ToolParam(description = "반드시 포함할 핵심 내용", required = true)
            String keyPoints) {
        return messageService.draft(situation, recipient, tone, keyPoints);
    }
}
