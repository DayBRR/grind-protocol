package com.davidrr.grindprotocol.userprofile.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.userprofile.dto.UpdateUserProfileRequest;
import com.davidrr.grindprotocol.userprofile.dto.UserProfileResponse;
import com.davidrr.grindprotocol.userprofile.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints para consultar y actualizar el perfil del usuario autenticado")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    @Operation(
            summary = "Obtener mi perfil",
            description = "Devuelve el perfil del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "userId": 1,
                                      "username": "david",
                                      "displayName": "David",
                                      "dailyTaskGoal": 3,
                                      "totalXp": 0,
                                      "corePoints": 0,
                                      "currentStreak": 0,
                                      "bestStreak": 0,
                                      "lastEvaluatedDate": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    })
    public UserProfileResponse getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return userProfileService.getCurrentUserProfile(currentUser.getId());
    }

    @PutMapping
    @Operation(
            summary = "Actualizar mi perfil",
            description = "Actualiza los campos editables del perfil del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil actualizado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "userId": 1,
                                      "username": "david",
                                      "displayName": "David Ruiz",
                                      "dailyTaskGoal": 5,
                                      "totalXp": 0,
                                      "corePoints": 0,
                                      "currentStreak": 0,
                                      "bestStreak": 0,
                                      "lastEvaluatedDate": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    })
    public UserProfileResponse updateMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos editables del perfil",
                    content = @Content(
                            schema = @Schema(implementation = UpdateUserProfileRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "displayName": "David Ruiz",
                                      "dailyTaskGoal": 5
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userProfileService.updateProfile(currentUser.getId(), request);
    }
}