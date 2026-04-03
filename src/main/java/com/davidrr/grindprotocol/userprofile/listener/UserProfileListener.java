package com.davidrr.grindprotocol.userprofile.listener;

import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.grindprotocol.userprofile.service.UserProfileService;
import com.davidrr.security.auth.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileListener {

    private final UserRepository userRepository;
    private final UserProfileService userProfileService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new IllegalStateException(
                        "User not found for id: " + event.userId()
                ));

        userProfileService.createDefaultProfile(user);

        log.info("Default UserProfile created for userId={}", event.userId());
    }
}