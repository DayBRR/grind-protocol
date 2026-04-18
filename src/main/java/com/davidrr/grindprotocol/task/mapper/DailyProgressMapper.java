package com.davidrr.grindprotocol.task.mapper;

import com.davidrr.grindprotocol.task.dto.DailyProgressResponse;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DailyProgressMapper {

    @Mapping(target = "userId", source = "user.id")
    DailyProgressResponse toResponse(DailyProgress progress);
}