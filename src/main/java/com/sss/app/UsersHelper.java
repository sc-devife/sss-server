package com.sss.app;

import com.sss.app.entity.User;
import com.sss.app.repository.UserRepository;
import org.springframework.stereotype.Component;
import com.sss.app.exception.NotFoundException;

@Component
public class UsersHelper {

    private final UserRepository userRepository;

    public UsersHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByUid(String uid) {
        return userRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("User not found with uid: " + uid));
    }
}

