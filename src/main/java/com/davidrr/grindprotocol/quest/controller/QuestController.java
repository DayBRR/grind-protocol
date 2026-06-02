package com.davidrr.grindprotocol.quest.controller;

import com.davidrr.grindprotocol.quest.dto.QuestClaimResponse;
import com.davidrr.grindprotocol.quest.dto.QuestResponse;
import com.davidrr.grindprotocol.quest.service.QuestService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/quests")
@RequiredArgsConstructor
@Tag(name = "Quests", description = "Quest progression and reward endpoints")
@SecurityRequirement(name = "bearerAuth")
public class QuestController {

    private final QuestService questService;

    @GetMapping
    @Operation(
            summary = "Get user quests",
            description = "Returns all enabled quests with the authenticated user's current period state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quests retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public List<QuestResponse> getQuests(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return questService.getQuests(currentUser.getId());
    }

    @PostMapping("/{questId}/claim")
    @Operation(
            summary = "Claim quest reward",
            description = "Claims the reward for a completed quest."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quest claimed successfully"),
            @ApiResponse(responseCode = "400", description = "Quest is not completed or already claimed"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "404", description = "Quest not found")
    })
    public QuestClaimResponse claimQuest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(
                    description = "Quest identifier",
                    example = "1"
            )
            @PathVariable Long questId
    ) {
        return questService.claimQuest(
                questId,
                currentUser.getId()
        );
    }
}