package com.agenticplayer.lifestyle.tool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.agenticplayer.lifestyle.service.ExpenseSettlementService;
import com.agenticplayer.lifestyle.service.LifeEventGuideService;
import com.agenticplayer.lifestyle.service.MealSuggestionService;
import com.agenticplayer.lifestyle.service.MessageDraftService;

class LifestyleAssistantToolsTest {

    private final LifestyleAssistantTools tools = new LifestyleAssistantTools(
            new MealSuggestionService(),
            new ExpenseSettlementService(),
            new LifeEventGuideService(),
            new MessageDraftService());

    @Test
    void mealSuggestionAcceptsMissingOptionalArguments() {
        var result = tools.suggestMeals("계란, 김치", null, null);

        assertThat(result.suggestions()).isNotEmpty();
    }

    @Test
    void lifeEventGuideAcceptsMissingAttendance() {
        var result = tools.guideLifeEvent("결혼식", "회사 동료", null);

        assertThat(result.eventType()).isEqualTo("결혼");
        assertThat(result.etiquette()).isNotEmpty();
    }

    @Test
    void messageDraftAcceptsMissingTone() {
        var result = tools.draftMessage("연차 재요청", "팀장님", null, "7월 3일 연차가 필요합니다");

        assertThat(result.recommended()).isNotBlank();
    }
}
