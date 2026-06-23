package com.agenticplayer.lifestyle.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageDraftServiceTest {

    private final MessageDraftService service = new MessageDraftService();

    @Test
    void doesNotDuplicateHonorific() {
        var result = service.draft(
                "연차 재요청",
                "팀장님",
                "정중한",
                "7월 3일 가족 일정으로 연차가 필요합니다");

        assertThat(result.recommended())
                .contains("팀장님,")
                .doesNotContain("팀장님님")
                .doesNotContain("필요합니다 사유로");
    }
}
