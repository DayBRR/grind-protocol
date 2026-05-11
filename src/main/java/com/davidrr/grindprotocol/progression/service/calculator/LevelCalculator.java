package com.davidrr.grindprotocol.progression.service.calculator;

import org.springframework.stereotype.Component;

@Component
public class LevelCalculator {

    private static final int XP_PER_LEVEL = 100;

    public int calculateLevel(long totalXp) {
        validateTotalXp(totalXp);

        return (int) (totalXp / XP_PER_LEVEL) + 1;
    }

    public long getXpRequiredForLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be greater than or equal to 1");
        }

        return (long) (level - 1) * XP_PER_LEVEL;
    }

    public long getXpForCurrentLevel(long totalXp) {
        int currentLevel = calculateLevel(totalXp);

        return getXpRequiredForLevel(currentLevel);
    }

    public long getXpForNextLevel(long totalXp) {
        int currentLevel = calculateLevel(totalXp);

        return getXpRequiredForLevel(currentLevel + 1);
    }

    public long getXpProgressInCurrentLevel(long totalXp) {
        validateTotalXp(totalXp);

        return totalXp - getXpForCurrentLevel(totalXp);
    }

    public long getXpRemainingForNextLevel(long totalXp) {
        validateTotalXp(totalXp);

        return getXpForNextLevel(totalXp) - totalXp;
    }

    private void validateTotalXp(long totalXp) {
        if (totalXp < 0) {
            throw new IllegalArgumentException("Total XP cannot be negative");
        }
    }
}