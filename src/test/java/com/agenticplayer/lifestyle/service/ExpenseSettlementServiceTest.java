package com.agenticplayer.lifestyle.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExpenseSettlementServiceTest {

    private final ExpenseSettlementService service = new ExpenseSettlementService();

    @Test
    void createsTransfersForEqualSplit() {
        var result = service.settle(
                "민수, 영희, 철수",
                """
                민수 20000
                영희 30000
                철수 10000
                """);

        assertThat(result.totalWon()).isEqualTo(60_000);
        assertThat(result.shareByPerson()).containsEntry("민수", 20_000L);
        assertThat(result.transfers()).hasSize(1);
        assertThat(result.transfers().get(0).from()).isEqualTo("철수");
        assertThat(result.transfers().get(0).to()).isEqualTo("영희");
        assertThat(result.transfers().get(0).amountWon()).isEqualTo(10_000);
    }

    @Test
    void reportsLinesThatCannotBeParsed() {
        var result = service.settle("민수, 영희", "민수 20000\n누가 결제했는지 모름");

        assertThat(result.warnings()).hasSize(1);
        assertThat(result.totalWon()).isEqualTo(20_000);
    }
}
