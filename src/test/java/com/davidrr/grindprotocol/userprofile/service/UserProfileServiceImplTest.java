package com.davidrr.grindprotocol.userprofile.service;

import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.dto.UpdateUserProfileRequest;
import com.davidrr.grindprotocol.userprofile.dto.UserProfileResponse;
import com.davidrr.grindprotocol.userprofile.mapper.UserProfileMapper;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.davidrr.grindprotocol.userprofile.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("david")
                .email("david@test.com")
                .password("secret")
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("createDefaultProfile debe crear perfil por defecto si no existe")
    void createDefaultProfile_shouldCreateProfileWhenNotExists() {
        when(userProfileRepository.existsByUserId(1L)).thenReturn(false);

        userProfileService.createDefaultProfile(user);

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(profileCaptor.capture());

        UserProfile saved = profileCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getDisplayName()).isEqualTo("david");
        assertThat(saved.getDailyTaskGoal()).isEqualTo(3);
        assertThat(saved.getTotalXp()).isZero();
        assertThat(saved.getCorePoints()).isZero();
        assertThat(saved.getCurrentStreak()).isZero();
        assertThat(saved.getBestStreak()).isZero();
        assertThat(saved.getLastEvaluatedDate()).isNull();
    }

    @Test
    @DisplayName("createDefaultProfile no debe crear perfil si ya existe")
    void createDefaultProfile_shouldDoNothingWhenExists() {
        when(userProfileRepository.existsByUserId(1L)).thenReturn(true);

        userProfileService.createDefaultProfile(user);

        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCurrentUserProfile debe devolver perfil mapeado")
    void getCurrentUserProfile_shouldReturnMappedProfile() {
        UserProfile profile = UserProfile.builder().user(user).displayName("David").dailyTaskGoal(3).build();
        UserProfileResponse response = mock(UserProfileResponse.class);

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userProfileMapper.toResponse(profile)).thenReturn(response);

        UserProfileResponse result = userProfileService.getCurrentUserProfile(1L);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("getCurrentUserProfile debe fallar si no existe el perfil")
    void getCurrentUserProfile_shouldThrowWhenNotFound() {
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getCurrentUserProfile(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateProfile debe aplicar cambios, guardar y mapear")
    void updateProfile_shouldUpdateAndReturnMappedProfile() {
        UpdateUserProfileRequest request = mock(UpdateUserProfileRequest.class);
        UserProfile profile = UserProfile.builder().user(user).displayName("David").dailyTaskGoal(3).build();
        UserProfileResponse response = mock(UserProfileResponse.class);

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(profile)).thenReturn(profile);
        when(userProfileMapper.toResponse(profile)).thenReturn(response);

        UserProfileResponse result = userProfileService.updateProfile(1L, request);

        verify(userProfileMapper).updateEntityFromRequest(request, profile);
        verify(userProfileRepository).save(profile);
        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("updateProfile debe fallar si no existe el perfil")
    void updateProfile_shouldThrowWhenProfileNotFound() {
        UpdateUserProfileRequest request = mock(UpdateUserProfileRequest.class);
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.updateProfile(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userProfileMapper, never()).updateEntityFromRequest(any(), any());
        verify(userProfileRepository, never()).save(any());
    }
}
