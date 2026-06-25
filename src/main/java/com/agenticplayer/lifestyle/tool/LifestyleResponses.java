package com.agenticplayer.lifestyle.tool;

import java.util.List;
import java.util.Map;

public final class LifestyleResponses {

    private LifestyleResponses() {
    }

    public record MealSuggestion(
            String name,
            int estimatedMinutes,
            List<String> availableIngredients,
            List<String> missingIngredients,
            List<String> steps,
            String reason) {
    }

    public record MealResult(
            List<String> recognizedIngredients,
            List<MealSuggestion> suggestions,
            String safetyNote) {
    }

    public record Transfer(String from, String to, long amountWon) {
    }

    public record SettlementResult(
            boolean complete,
            String summary,
            List<String> participants,
            long totalWon,
            Map<String, Long> paidByPerson,
            Map<String, Long> shareByPerson,
            List<Transfer> transfers,
            List<String> warnings,
            List<String> nextQuestions) {
    }

    public record LifeEventGuide(
            String eventType,
            String relationship,
            List<String> amountOptions,
            List<String> etiquette,
            List<String> messageExamples,
            String caution) {
    }

    public record MessageDraftResult(
            String recommended,
            List<String> alternatives,
            List<String> editingTips) {
    }

    public record OutdoorSafetyBriefing(
            String locationName,
            double latitude,
            double longitude,
            String activity,
            String riskLevel,
            String recommendation,
            WeatherSnapshot weather,
            AirQualitySnapshot airQuality,
            List<String> reasons,
            List<String> checklist,
            List<String> cautions,
            List<String> sources) {
    }

    public record WeatherSnapshot(
            double temperatureCelsius,
            double apparentTemperatureCelsius,
            int humidityPercent,
            double precipitationMm,
            int precipitationProbabilityPercent,
            double uvIndex,
            double windSpeedKmh,
            String weatherCodeDescription) {
    }

    public record AirQualitySnapshot(
            double pm10,
            double pm25,
            int usAqi,
            String grade) {
    }
}
