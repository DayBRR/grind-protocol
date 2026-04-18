package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.DailyProgressResponse;
import com.davidrr.grindprotocol.task.service.DailyProgressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/daily-progress")
@RequiredArgsConstructor
@Tag(name = "Daily Progress", description = "Progreso diario del usuario autenticado")
public class DailyProgressController {

    private final DailyProgressService dailyProgressService;

    @GetMapping("/today")
    public DailyProgressResponse getTodayProgress(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return dailyProgressService.getTodayProgress(currentUser.getId());
    }

}