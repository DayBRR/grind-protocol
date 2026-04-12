package com.davidrr.grindprotocol.task.mapper;

import com.davidrr.grindprotocol.task.dto.TaskResponse;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.Trait;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "templateId", source = "template.id")
    @Mapping(target = "traitCodes", expression = "java(mapTraitCodes(task.getTraits()))")
    TaskResponse toResponse(Task task);

    default Set<String> mapTraitCodes(Set<Trait> traits) {
        if (traits == null) {
            return new LinkedHashSet<>();
        }

        return traits.stream()
                .map(Trait::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}