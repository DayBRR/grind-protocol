package com.davidrr.grindprotocol.progression.service.calculator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LevelCalculatorTest {

    private final LevelCalculator levelCalculator = new LevelCalculator();

    @Test
    @DisplayName("Debe devolver nivel 1 cuando la XP total es 0")
    void calculateLevel_shouldReturnLevelOneWhenTotalXpIsZero() {
        int level = levelCalculator.calculateLevel(0L);

        assertThat(level).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe mantener nivel 1 antes de llegar a 100 XP")
    void calculateLevel_shouldReturnLevelOneBeforeFirstThreshold() {
        int level = levelCalculator.calculateLevel(99L);

        assertThat(level).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe devolver nivel 2 al llegar a 100 XP")
    void calculateLevel_shouldReturnLevelTwoAtOneHundredXp() {
        int level = levelCalculator.calculateLevel(100L);

        assertThat(level).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe devolver nivel 3 al llegar a 200 XP")
    void calculateLevel_shouldReturnLevelThreeAtTwoHundredXp() {
        int level = levelCalculator.calculateLevel(200L);

        assertThat(level).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe devolver la XP mínima necesaria para un nivel")
    void getXpRequiredForLevel_shouldReturnRequiredXpForLevel() {
        assertThat(levelCalculator.getXpRequiredForLevel(1)).isEqualTo(0L);
        assertThat(levelCalculator.getXpRequiredForLevel(2)).isEqualTo(100L);
        assertThat(levelCalculator.getXpRequiredForLevel(3)).isEqualTo(200L);
        assertThat(levelCalculator.getXpRequiredForLevel(10)).isEqualTo(900L);
    }

    @Test
    @DisplayName("Debe devolver la XP inicial del nivel actual")
    void getXpForCurrentLevel_shouldReturnCurrentLevelStartXp() {
        assertThat(levelCalculator.getXpForCurrentLevel(0L)).isEqualTo(0L);
        assertThat(levelCalculator.getXpForCurrentLevel(99L)).isEqualTo(0L);
        assertThat(levelCalculator.getXpForCurrentLevel(100L)).isEqualTo(100L);
        assertThat(levelCalculator.getXpForCurrentLevel(250L)).isEqualTo(200L);
    }

    @Test
    @DisplayName("Debe devolver la XP necesaria para el siguiente nivel")
    void getXpForNextLevel_shouldReturnNextLevelXp() {
        assertThat(levelCalculator.getXpForNextLevel(0L)).isEqualTo(100L);
        assertThat(levelCalculator.getXpForNextLevel(99L)).isEqualTo(100L);
        assertThat(levelCalculator.getXpForNextLevel(100L)).isEqualTo(200L);
        assertThat(levelCalculator.getXpForNextLevel(250L)).isEqualTo(300L);
    }

    @Test
    @DisplayName("Debe devolver el progreso de XP dentro del nivel actual")
    void getXpProgressInCurrentLevel_shouldReturnProgressInsideCurrentLevel() {
        assertThat(levelCalculator.getXpProgressInCurrentLevel(0L)).isEqualTo(0L);
        assertThat(levelCalculator.getXpProgressInCurrentLevel(50L)).isEqualTo(50L);
        assertThat(levelCalculator.getXpProgressInCurrentLevel(100L)).isEqualTo(0L);
        assertThat(levelCalculator.getXpProgressInCurrentLevel(250L)).isEqualTo(50L);
    }

    @Test
    @DisplayName("Debe devolver la XP restante para el siguiente nivel")
    void getXpRemainingForNextLevel_shouldReturnRemainingXpForNextLevel() {
        assertThat(levelCalculator.getXpRemainingForNextLevel(0L)).isEqualTo(100L);
        assertThat(levelCalculator.getXpRemainingForNextLevel(50L)).isEqualTo(50L);
        assertThat(levelCalculator.getXpRemainingForNextLevel(100L)).isEqualTo(100L);
        assertThat(levelCalculator.getXpRemainingForNextLevel(250L)).isEqualTo(50L);
    }

    @Test
    @DisplayName("Debe fallar si la XP total es negativa")
    void calculateLevel_shouldThrowWhenTotalXpIsNegative() {
        assertThatThrownBy(() -> levelCalculator.calculateLevel(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Total XP cannot be negative");
    }

    @Test
    @DisplayName("Debe fallar si el nivel es menor que 1")
    void getXpRequiredForLevel_shouldThrowWhenLevelIsLowerThanOne() {
        assertThatThrownBy(() -> levelCalculator.getXpRequiredForLevel(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Level must be greater than or equal to 1");
    }
}