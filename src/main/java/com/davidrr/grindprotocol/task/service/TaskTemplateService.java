package com.davidrr.grindprotocol.task.service;

import com.davidrr.grindprotocol.task.dto.CreateTaskTemplateRequest;
import com.davidrr.grindprotocol.task.dto.TaskTemplateResponse;

import java.util.List;

public interface TaskTemplateService {

    TaskTemplateResponse createTemplate(Long userId, CreateTaskTemplateRequest request);

    List<TaskTemplateResponse> getMyActiveTemplates(Long userId);

    List<TaskTemplateResponse> getPublicActiveTemplates();

    TaskTemplateResponse getMyTemplateById(Long userId, Long templateId);

    TaskTemplateResponse getPublicTemplateById(Long templateId);
}