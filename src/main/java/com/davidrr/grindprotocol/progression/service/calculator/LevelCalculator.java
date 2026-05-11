package com.davidrr.grindprotocol.progression.service.calculator;

import org.springframework.stereotype.Component;

@Component
public class LevelCalculator {

    private static final int XP_PER_LEVEL = 100;

    public int calculateLevel(long totalXp) {
        if (totalXp < 0) {
            throw new IllegalArgumentException("Total XP cannot be negative");
        }

        return (int) (totalXp / XP_PER_LEVEL) + 1;
    }
}