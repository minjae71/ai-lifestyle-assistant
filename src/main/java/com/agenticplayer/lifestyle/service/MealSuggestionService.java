package com.agenticplayer.lifestyle.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.agenticplayer.lifestyle.tool.LifestyleResponses.MealResult;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.MealSuggestion;

@Service
public class MealSuggestionService {

    private static final List<Recipe> RECIPES = List.of(
            new Recipe("참치마요 덮밥", 10, List.of("참치", "밥", "마요네즈"),
                    List.of("참치의 기름을 뺍니다.", "밥 위에 참치와 마요네즈를 올립니다.", "김이나 간장을 취향껏 더합니다.")),
            new Recipe("계란 볶음밥", 15, List.of("계란", "밥", "대파"),
                    List.of("계란을 먼저 부드럽게 볶습니다.", "밥과 대파를 넣고 센 불에 볶습니다.", "소금이나 간장으로 간합니다.")),
            new Recipe("김치 계란 덮밥", 12, List.of("김치", "계란", "밥"),
                    List.of("김치를 먹기 좋게 잘라 볶습니다.", "계란을 넣어 함께 익힙니다.", "밥 위에 올립니다.")),
            new Recipe("양파 계란국", 15, List.of("양파", "계란", "국간장"),
                    List.of("물에 양파를 넣고 끓입니다.", "국간장으로 간합니다.", "푼 계란을 둘러 넣고 익힙니다.")),
            new Recipe("두부 김치볶음", 20, List.of("두부", "김치", "대파"),
                    List.of("두부를 데우거나 노릇하게 굽습니다.", "김치와 대파를 볶습니다.", "두부와 볶은 김치를 함께 담습니다.")),
            new Recipe("감자 양파전", 20, List.of("감자", "양파", "부침가루"),
                    List.of("감자와 양파를 얇게 채 썹니다.", "부침가루와 물을 섞습니다.", "팬에 얇게 펴 앞뒤로 굽습니다.")),
            new Recipe("토마토 계란볶음", 15, List.of("토마토", "계란", "대파"),
                    List.of("계란을 부드럽게 볶아 덜어둡니다.", "토마토와 대파를 볶습니다.", "계란을 다시 넣고 소금으로 간합니다.")),
            new Recipe("간장 비빔국수", 12, List.of("소면", "간장", "참기름"),
                    List.of("소면을 삶아 찬물에 헹굽니다.", "간장과 참기름으로 양념합니다.", "김가루나 계란을 더합니다."))
    );

    public MealResult suggest(String ingredientText, int maxMinutes, String preference) {
        Set<String> ingredients = parseIngredients(ingredientText);
        int timeLimit = maxMinutes <= 0 ? 30 : Math.min(maxMinutes, 120);
        String normalizedPreference = preference == null ? "" : preference.trim().toLowerCase(Locale.ROOT);

        List<MealSuggestion> suggestions = RECIPES.stream()
                .filter(recipe -> recipe.minutes() <= timeLimit)
                .map(recipe -> scoredSuggestion(recipe, ingredients, normalizedPreference))
                .sorted(Comparator.comparingInt(ScoredSuggestion::score).reversed()
                        .thenComparingInt(item -> item.suggestion().estimatedMinutes()))
                .limit(3)
                .map(ScoredSuggestion::suggestion)
                .toList();

        return new MealResult(
                new ArrayList<>(ingredients),
                suggestions,
                "알레르기, 식재료 변질 여부와 충분한 가열 상태는 사용자가 직접 확인해야 합니다.");
    }

    private ScoredSuggestion scoredSuggestion(Recipe recipe, Set<String> ingredients, String preference) {
        List<String> available = recipe.ingredients().stream().filter(ingredients::contains).toList();
        List<String> missing = recipe.ingredients().stream().filter(item -> !ingredients.contains(item)).toList();
        int score = available.size() * 10 - missing.size() * 4;
        if (!preference.isBlank() && recipe.name().toLowerCase(Locale.ROOT).contains(preference)) {
            score += 3;
        }
        String reason = missing.isEmpty()
                ? "입력한 재료만으로 기본 조리가 가능합니다."
                : "핵심 재료 " + available.size() + "개가 있고, " + String.join(", ", missing) + "만 보충하면 됩니다.";
        return new ScoredSuggestion(score, new MealSuggestion(
                recipe.name(),
                recipe.minutes(),
                available,
                missing,
                recipe.steps(),
                reason));
    }

    private Set<String> parseIngredients(String text) {
        if (text == null || text.isBlank()) {
            return new LinkedHashSet<>();
        }
        Set<String> result = new LinkedHashSet<>();
        Arrays.stream(text.split("[,，/\\n;]+"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .forEach(value -> {
                    result.add(value);
                    if (value.endsWith("밖에 없어")) {
                        result.add(value.substring(0, value.length() - "밖에 없어".length()).trim());
                    }
                });
        return result;
    }

    private record Recipe(String name, int minutes, List<String> ingredients, List<String> steps) {
    }

    private record ScoredSuggestion(int score, MealSuggestion suggestion) {
    }
}
