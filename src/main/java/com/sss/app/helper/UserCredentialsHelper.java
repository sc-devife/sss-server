package com.sss.app.helper;

import com.sss.app.entity.UserCredential;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.UserCredentialRepository;
import org.springframework.stereotype.Component;

@Component
public class UserCredentialsHelper {

    private final UserCredentialRepository userCredentialRepository;

    public UserCredentialsHelper(UserCredentialRepository userCredentialRepository) {
        this.userCredentialRepository = userCredentialRepository;
    }

    public UserCredential getUserCredentialBySeqa(Long seqa) {
        return userCredentialRepository.findBySeqa(seqa)
                .orElseThrow(() -> new NotFoundException("Credentials not found."));
    }
}

