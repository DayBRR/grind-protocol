package com.davidrr.grindprotocol.reward.mapper;

import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;
import com.davidrr.grindprotocol.reward.dto.RewardResponse;
import com.davidrr.grindprotocol.reward.model.Reward;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RewardMapper {

    RewardResponse toResponse(Reward reward);

    @Mapping(target = "rewardId", source = "reward.id")
    @Mapping(target = "rewardName", source = "reward.name")
    @Mapping(target = "rewardDescription", source = "reward.description")
    @Mapping(target = "rewardType", source = "reward.type")
    @Mapping(target = "rewardCategory", source = "reward.category")
    RewardRedemptionResponse toRedemptionResponse(RewardRedemption redemption);
}