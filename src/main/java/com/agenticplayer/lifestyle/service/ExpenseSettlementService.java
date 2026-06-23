package com.agenticplayer.lifestyle.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.agenticplayer.lifestyle.tool.LifestyleResponses.SettlementResult;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.Transfer;

@Service
public class ExpenseSettlementService {

    private static final Pattern EXPENSE_PATTERN = Pattern.compile(
            "^\\s*([^:=,\\d]+?)\\s*(?:[:=,]|\\s+)\\s*([\\d,]+)\\s*(?:원)?\\s*$");
    private static final long MAX_EXPENSE_WON = 1_000_000_000_000L;

    public SettlementResult settle(String participantsText, String expenseLines) {
        Set<String> participants = parseParticipants(participantsText);
        Map<String, Long> paid = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>();

        if (expenseLines != null) {
            for (String line : expenseLines.split("\\R|;")) {
                if (line.isBlank()) {
                    continue;
                }
                Matcher matcher = EXPENSE_PATTERN.matcher(line.trim());
                if (!matcher.matches()) {
                    warnings.add("해석하지 못한 지출 내역: " + line.trim());
                    continue;
                }

                String name = matcher.group(1).trim();
                Long amount = parseAmount(matcher.group(2), line, warnings);
                if (amount == null) {
                    continue;
                }

                long current = paid.getOrDefault(name, 0L);
                if (current > MAX_EXPENSE_WON - amount) {
                    warnings.add("허용 범위를 초과한 누적 금액: " + line.trim());
                    continue;
                }

                participants.add(name);
                paid.put(name, current + amount);
            }
        }

        if (participants.isEmpty()) {
            return new SettlementResult(List.of(), 0, Map.of(), Map.of(), List.of(),
                    List.of("참여자와 지출 내역을 확인할 수 없습니다."));
        }

        List<String> orderedParticipants = new ArrayList<>(participants);
        orderedParticipants.forEach(name -> paid.putIfAbsent(name, 0L));
        long total = paid.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Long> shares = allocateShares(orderedParticipants, total);
        List<Transfer> transfers = calculateTransfers(orderedParticipants, paid, shares);

        if (total == 0) {
            warnings.add("정산할 결제 금액이 0원입니다.");
        }

        return new SettlementResult(
                orderedParticipants,
                total,
                paid,
                shares,
                transfers,
                warnings);
    }

    private Long parseAmount(String amountText, String originalLine, List<String> warnings) {
        String normalized = amountText.replace(",", "");
        try {
            long amount = Long.parseLong(normalized);
            if (amount > MAX_EXPENSE_WON) {
                warnings.add("허용 범위를 초과한 금액: " + originalLine.trim());
                return null;
            }
            return amount;
        } catch (NumberFormatException exception) {
            warnings.add("숫자로 변환할 수 없는 금액: " + originalLine.trim());
            return null;
        }
    }

    private Set<String> parseParticipants(String text) {
        Set<String> participants = new LinkedHashSet<>();
        if (text == null || text.isBlank()) {
            return participants;
        }
        for (String token : text.split("[,，/\\n;\\s]+")) {
            String name = token.trim();
            if (!name.isBlank()) {
                participants.add(name);
            }
        }
        return participants;
    }

    private Map<String, Long> allocateShares(List<String> participants, long total) {
        Map<String, Long> shares = new LinkedHashMap<>();
        long base = total / participants.size();
        long remainder = total % participants.size();
        for (int i = 0; i < participants.size(); i++) {
            shares.put(participants.get(i), base + (i < remainder ? 1 : 0));
        }
        return shares;
    }

    private List<Transfer> calculateTransfers(
            List<String> participants,
            Map<String, Long> paid,
            Map<String, Long> shares) {
        Queue<Balance> creditors = new ArrayDeque<>();
        Queue<Balance> debtors = new ArrayDeque<>();

        for (String name : participants) {
            long balance = paid.get(name) - shares.get(name);
            if (balance > 0) {
                creditors.add(new Balance(name, balance));
            } else if (balance < 0) {
                debtors.add(new Balance(name, -balance));
            }
        }

        List<Transfer> transfers = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Balance creditor = creditors.remove();
            Balance debtor = debtors.remove();
            long amount = Math.min(creditor.amount(), debtor.amount());
            transfers.add(new Transfer(debtor.name(), creditor.name(), amount));

            if (creditor.amount() > amount) {
                creditors.add(new Balance(creditor.name(), creditor.amount() - amount));
            }
            if (debtor.amount() > amount) {
                debtors.add(new Balance(debtor.name(), debtor.amount() - amount));
            }
        }
        return transfers;
    }

    private record Balance(String name, long amount) {
    }
}
