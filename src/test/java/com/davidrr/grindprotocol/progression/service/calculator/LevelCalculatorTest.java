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
    @DisplayName("Debe fallar si la XP total es negativa")
    void calculateLevel_shouldThrowWhenTotalXpIsNegative() {
        assertThatThrownBy(() -> levelCalculator.calculateLevel(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Total XP cannot be negative");
    }
}