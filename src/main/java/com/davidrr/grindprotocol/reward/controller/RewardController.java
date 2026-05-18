package com.davidrr.grindprotocol.reward.controller;

import com.davidrr.grindprotocol.reward.dto.RewardRedeemResponse;
import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;
import com.davidrr.grindprotocol.reward.dto.RewardResponse;
import com.davidrr.grindprotocol.reward.service.RewardRedemptionService;
import com.davidrr.grindprotocol.reward.service.RewardService;
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
@RequestMapping("/me/rewards")
@RequiredArgsConstructor
@Tag(name = "Rewards", description = "Reward store and Core Points redemption endpoints")
@SecurityRequirement(name = "bearerAuth")
public class RewardController {

    private final RewardService rewardService;
    private final RewardRedemptionService rewardRedemptionService;

    @GetMapping
    @Operation(
            summary = "Get available rewards",
            description = "Returns all enabled rewards that can be displayed in the user reward store."
    )
    public List<RewardResponse> getAvailableRewards() {
        return rewardService.getAvailableRewards();
    }

    @GetMapping("/redemptions")
    @Operation(
            summary = "Get my reward redemptions",
            description = "Returns the authenticated user's reward redemption history."
    )
    public List<RewardRedemptionResponse> getMyRedemptions(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return rewardService.getMyRedemptions(currentUser.getId());
    }

    @PostMapping("/{rewardId}/redeem")
    @Operation(
            summary = "Redeem reward",
            description = "Redeems an enabled reward by spending Core Points from the authenticated user's profile."
    )
    public RewardRedeemResponse redeemReward(
            @PathVariable Long rewardId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {

        return rewardRedemptionService.redeemReward(
                rewardId,
                currentUser.getId()
        );
    }
}