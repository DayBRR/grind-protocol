package com.davidrr.grindprotocol.task.mapper;

import com.davidrr.grindprotocol.task.dto.TaskCompletionResponse;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskCompletionMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "userId", source = "user.id")
    TaskCompletionResponse toResponse(TaskCompletion completion);
}