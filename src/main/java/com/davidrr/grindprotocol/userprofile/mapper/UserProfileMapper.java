package com.davidrr.grindprotocol.userprofile.mapper;

import com.davidrr.grindprotocol.userprofile.dto.UpdateUserProfileRequest;
import com.davidrr.grindprotocol.userprofile.dto.UserProfileResponse;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    UserProfileResponse toResponse(UserProfile profile);

    void updateEntityFromRequest(UpdateUserProfileRequest request, @MappingTarget UserProfile profile);
}