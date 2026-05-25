package com.davidrr.grindprotocol.achievement.controller;

import com.davidrr.grindprotocol.achievement.dto.AchievementClaimResponse;
import com.davidrr.grindprotocol.achievement.dto.AchievementResponse;
import com.davidrr.grindprotocol.achievement.service.AchievementService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/achievements")
@RequiredArgsConstructor
@Tag(
        name = "Achievements",
        description = "Achievement progression and rewards endpoints"
)
@SecurityRequirement(name = "bearerAuth")
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    @Operation(
            summary = "Get user achievements",
            description = "Returns all enabled achievements with user progression state."
    )
    public List<AchievementResponse> getAchievements(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {

        return achievementService.getAchievements(
                currentUser.getId()
        );
    }

    @PostMapping("/evaluate")
    @Operation(
            summary = "Evaluate achievements",
            description = "Triggers achievement evaluation for the authenticated user."
    )
    public void evaluateAchievements(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            AuthenticatedUser currentUser
    ) {

        achievementService.evaluateAchievements(
                currentUser.getId()
        );
    }

    @PostMapping("/{achievementId}/claim")
    @Operation(
            summary = "Claim achievement reward",
            description = "Claims XP and Core Points rewards from an unlocked achievement."
    )
    public AchievementClaimResponse claimAchievement(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            AuthenticatedUser currentUser,
            @PathVariable Long achievementId
    ) {
        return achievementService.claimAchievement(
                achievementId,
                currentUser.getId()
        );
    }
}