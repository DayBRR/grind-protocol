package com.davidrr.grindprotocol.reward.controller;

import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;
import com.davidrr.grindprotocol.reward.dto.RewardResponse;
import com.davidrr.grindprotocol.reward.service.RewardService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @GetMapping
    @Operation(summary = "Get available rewards")
    public List<RewardResponse> getAvailableRewards() {
        return rewardService.getAvailableRewards();
    }

    @GetMapping("/redemptions")
    @Operation(summary = "Get my reward redemptions")
    public List<RewardRedemptionResponse> getMyRedemptions(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return rewardService.getMyRedemptions(currentUser.getId());
    }
}