package com.agenticplayer.lifestyle.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.agenticplayer.lifestyle.tool.LifestyleResponses.MessageDraftResult;

@Service
public class MessageDraftService {

    public MessageDraftResult draft(
            String situation,
            String recipient,
            String tone,
            String keyPoints) {
        String safeSituation = blankToDefault(situation, "전달할 상황");
        String safeRecipient = blankToDefault(recipient, "상대방");
        String recipientWithSuffix = addHonorific(safeRecipient);
        String safeTone = blankToDefault(tone, "정중한");
        String safePoints = trimSentenceEnding(blankToDefault(keyPoints, "필요한 내용을 확인 부탁드립니다"));
        String normalized = safeSituation.toLowerCase(Locale.ROOT);

        String recommended;
        List<String> alternatives = new ArrayList<>();

        if (containsAny(normalized, "연차", "휴가")) {
            recommended = buildLeaveRequest(recipientWithSuffix, safePoints);
            alternatives.add("안녕하세요. 말씀드린 연차 일정과 관련해 다시 한번 가능 여부를 여쭙습니다. 일정 조정이 필요하다면 가능한 대안을 함께 맞추겠습니다.");
        } else if (containsAny(normalized, "사과", "미안", "실수")) {
            recommended = recipientWithSuffix + ", " + safePoints
                    + " 부분은 제 확인이 부족했습니다. 불편을 드려 죄송합니다. 같은 일이 반복되지 않도록 바로 보완하겠습니다.";
            alternatives.add("제가 놓친 부분 때문에 불편을 드렸습니다. 변명하지 않고 필요한 조치를 먼저 하겠습니다. 죄송합니다.");
        } else if (containsAny(normalized, "거절", "부탁")) {
            recommended = "말씀해주셔서 감사합니다. 다만 현재는 " + safePoints
                    + " 때문에 요청을 맡기 어려울 것 같습니다. 기대에 바로 답하지 못해 죄송합니다.";
            alternatives.add("좋은 제안 감사해요. 이번에는 여건이 맞지 않아 함께하기 어렵지만, 다음 기회에 다시 이야기 나눌 수 있으면 좋겠습니다.");
        } else if (containsAny(normalized, "약속", "일정", "변경")) {
            recommended = recipientWithSuffix + ", 죄송하지만 " + safePoints
                    + " 때문에 기존 일정을 조정할 수 있을지 여쭙습니다. 가능한 시간을 알려주시면 최대한 맞추겠습니다.";
            alternatives.add("일정 변경을 부탁드리게 되어 죄송합니다. 괜찮으시다면 서로 가능한 시간을 다시 맞춰보고 싶습니다.");
        } else {
            recommended = recipientWithSuffix + ", 안녕하세요. " + safeSituation + " 관련해 연락드립니다. "
                    + safePoints + ". 확인하신 뒤 편하실 때 답변 부탁드립니다.";
            alternatives.add(safeSituation + " 건으로 연락드렸습니다. " + safePoints + ". 감사합니다.");
        }

        if (safeTone.contains("친근") || safeTone.contains("편한")) {
            alternatives.add("안녕하세요! " + safeSituation + " 때문에 연락드렸어요. " + safePoints + ". 편할 때 알려주세요.");
        } else if (safeTone.contains("간결") || safeTone.contains("짧")) {
            alternatives.add(safeSituation + " 관련 요청드립니다. " + safePoints + ". 확인 부탁드립니다.");
        }

        return new MessageDraftResult(
                recommended,
                alternatives,
                List.of(
                        "실제 날짜·시간·이름을 넣어 보내기 전에 한 번 확인하세요.",
                        "상대방의 잘못을 단정하기보다 필요한 행동을 구체적으로 적으세요.",
                        "민감한 개인정보나 회사 기밀은 메시지에 넣지 마세요."));
    }

    private String addHonorific(String recipient) {
        return recipient.endsWith("님") ? recipient : recipient + "님";
    }

    private String buildLeaveRequest(String recipientWithSuffix, String safePoints) {
        List<String> sentences = new ArrayList<>();
        sentences.add("안녕하세요. " + recipientWithSuffix + ", " + safePoints);

        if (!containsAny(safePoints, "연차", "휴가")) {
            sentences.add("해당 일정에 연차 사용을 요청드립니다");
        }
        if (!containsAny(safePoints, "인수인계", "업무 공백", "업무공백")) {
            sentences.add("업무 공백이 없도록 필요한 인수인계를 미리 준비하겠습니다");
        }
        sentences.add("검토 부탁드립니다");

        return sentences.stream()
                .map(this::trimSentenceEnding)
                .reduce((left, right) -> left + ". " + right)
                .orElse("검토 부탁드립니다")
                + ".";
    }

    private String trimSentenceEnding(String value) {
        return value.replaceFirst("[.!?。！？]+$", "");
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
