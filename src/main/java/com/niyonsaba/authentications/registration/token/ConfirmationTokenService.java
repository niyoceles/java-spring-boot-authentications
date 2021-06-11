package com.niyonsaba.authentications.registration.token;

import com.niyonsaba.authentications.appuser.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public ConfirmationToken getUser(Long id) {
        return confirmationTokenRepository.findByAppUserId(id);
    }

    public int setConfirmedAt(String token) {
        return confirmationTokenRepository.updateConfirmedAt(
                token, LocalDateTime.now());
    }


    public void updateToken(AppUser appUser, ConfirmationToken newConfirmationToken) {
        ConfirmationToken currentConfirmationToken = confirmationTokenRepository.findByAppUserId(appUser.getId());
        currentConfirmationToken.setToken(newConfirmationToken.getToken());
        currentConfirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(currentConfirmationToken);
    }
}