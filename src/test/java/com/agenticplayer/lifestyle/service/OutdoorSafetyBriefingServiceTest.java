package com.agenticplayer.lifestyle.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.agenticplayer.lifestyle.service.OutdoorSafetyBriefingService.ResolvedLocation;
import com.agenticplayer.lifestyle.service.OutdoorSafetyBriefingService.SafetyDataClient;
import com.agenticplayer.lifestyle.service.OutdoorSafetyBriefingService.SafetyMeasurements;

class OutdoorSafetyBriefingServiceTest {

    @Test
    void recommendsOutdoorActivityWhenConditionsAreGood() {
        var service = new OutdoorSafetyBriefingService(new FakeSafetyDataClient(
                new SafetyMeasurements(22, 23, 45, 0, 10, 3, 8, 1, 20, 8, 35)));

        var result = service.brief("서울", null, null, "산책");

        assertThat(result.locationName()).contains("서울");
        assertThat(result.riskLevel()).isEqualTo("좋음");
        assertThat(result.recommendation()).contains("무난");
        assertThat(result.checklist()).isNotEmpty();
    }

    @Test
    void raisesRiskForExerciseWhenAirQualityIsBad() {
        var service = new OutdoorSafetyBriefingService(new FakeSafetyDataClient(
                new SafetyMeasurements(28, 30, 70, 0, 20, 5, 12, 2, 80, 40, 120)));

        var result = service.brief("마포구", null, null, "러닝");

        assertThat(result.riskLevel()).isEqualTo("위험");
        assertThat(result.reasons()).anyMatch(reason -> reason.contains("대기질"));
        assertThat(result.recommendation()).contains("실내 대안");
        assertThat(result.checklist()).anyMatch(item -> item.contains("마스크"));
    }

    @Test
    void usesCoordinatesWithoutGeocoding() {
        var fakeClient = new FakeSafetyDataClient(
                new SafetyMeasurements(20, 20, 50, 1, 60, 2, 5, 61, 15, 7, 30));
        var service = new OutdoorSafetyBriefingService(fakeClient);

        var result = service.brief(null, 37.5665, 126.9780, "아이 등원");

        assertThat(fakeClient.geocodeCalled).isFalse();
        assertThat(result.locationName()).isEqualTo("입력 좌표");
        assertThat(result.reasons()).anyMatch(reason -> reason.contains("강수"));
    }

    private static final class FakeSafetyDataClient implements SafetyDataClient {

        private final SafetyMeasurements measurements;
        private boolean geocodeCalled;

        private FakeSafetyDataClient(SafetyMeasurements measurements) {
            this.measurements = measurements;
        }

        @Override
        public ResolvedLocation geocode(String query) {
            geocodeCalled = true;
            return new ResolvedLocation(query + ", 대한민국", 37.5665, 126.9780);
        }

        @Override
        public SafetyMeasurements fetch(double latitude, double longitude) {
            return measurements;
        }
    }
}
