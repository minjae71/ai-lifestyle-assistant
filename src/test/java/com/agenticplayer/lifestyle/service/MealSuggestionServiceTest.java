package com.agenticplayer.lifestyle.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MealSuggestionServiceTest {

    private final MealSuggestionService service = new MealSuggestionService();

    @Test
    void ranksRecipeByAvailableIngredientsAndTime() {
        var result = service.suggest("계란, 밥, 대파", 15, "");

        assertThat(result.suggestions()).isNotEmpty();
        assertThat(result.suggestions().get(0).name()).isEqualTo("계란 볶음밥");
        assertThat(result.suggestions().get(0).missingIngredients()).isEmpty();
    }
}
