package com.niyonsaba.authentications.appuser;

import com.niyonsaba.authentications.email.BuildEmail;
import com.niyonsaba.authentications.email.EmailSender;
import com.niyonsaba.authentications.registration.token.ConfirmationToken;
import com.niyonsaba.authentications.registration.token.ConfirmationTokenService;
import javassist.compiler.ast.Stmnt;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {
    private final static String USER_NOT_FOUND_MSG =
            "user with email %s not found";
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final BuildEmail buildEmail;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
    }


    public String signUpUser(AppUser appUser) {
        try {
            boolean userExists = appUserRepository
                    .findByEmail(appUser.getEmail())
                    .isPresent();
            final Optional<AppUser> user = appUserRepository.findByEmail(appUser.getEmail());
            boolean userEnabled = appUserRepository
                    .findByEnabled(false)
                    .isPresent();



            if (userExists && userEnabled) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>||:"+ user.get().getEmail());

                String token = UUID.randomUUID().toString();
                String link = "http://localhost:8080/api/v1/registration/confirm?token=" + token;

                ConfirmationToken confirmationToken = new ConfirmationToken(
                        token,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(15),
                        user.get()
                );

                confirmationTokenService.updateToken(user.get(), confirmationToken);
                emailSender.send(
                        user.get().getEmail(),
                        buildEmail.sendEmail(user.get().getFirstName(), link));
                return "We have sent an email for confirmation" ;
            }

        } catch(NoSuchElementException e) {

        }

        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(
                confirmationToken);

//        TODO: SEND EMAIL

        return token ;
    }
    
    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
