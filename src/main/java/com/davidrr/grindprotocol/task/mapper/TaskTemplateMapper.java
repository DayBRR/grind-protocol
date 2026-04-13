package com.davidrr.grindprotocol.task.mapper;

import com.davidrr.grindprotocol.task.dto.TaskTemplateResponse;
import com.davidrr.grindprotocol.task.model.TaskTemplate;
import com.davidrr.grindprotocol.task.model.Trait;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TaskTemplateMapper {

    @Mapping(target = "creatorUserId", expression = "java(template.getCreatorUser() != null ? template.getCreatorUser().getId() : null)")
    @Mapping(target = "traitCodes", expression = "java(mapTraitCodes(template.getTraits()))")
    TaskTemplateResponse toResponse(TaskTemplate template);

    default Set<String> mapTraitCodes(Set<Trait> traits) {
        if (traits == null) {
            return new LinkedHashSet<>();
        }

        return traits.stream()
                .map(Trait::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}