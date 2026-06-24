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

    @Test
    void doesNotRepeatLeaveRequestDetailsAlreadyProvidedByUser() {
        var result = service.draft(
                "연차 요청",
                "팀장님",
                "정중한",
                "7월 3일에 연차를 사용하고 싶습니다. 업무 인수인계는 미리 준비하겠습니다.");

        assertThat(result.recommended())
                .contains("7월 3일에 연차를 사용하고 싶습니다")
                .contains("업무 인수인계는 미리 준비하겠습니다")
                .doesNotContain("연차 사용을 다시 요청드립니다")
                .doesNotContain("업무 공백이 없도록 필요한 인수인계를 미리 준비하겠습니다");
        assertThat(countOccurrences(result.recommended(), "연차")).isEqualTo(1);
        assertThat(countOccurrences(result.recommended(), "인수인계")).isEqualTo(1);
    }

    private int countOccurrences(String value, String search) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(search, index)) >= 0) {
            count++;
            index += search.length();
        }
        return count;
    }
}
