package com.sss.app.helper;

import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class UsersHelper {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public UsersHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByUid(String uid) {
        return userRepository.findUserWithRoles(uid)
                .orElseThrow(() -> new NotFoundException("User not found with uid: " + uid));
    }

    @Transactional
    public User createUser(UserCreateRequestDto dto) {
        System.out.println("Creating user with dto: " + dto);
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email is already in use");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getEmail());
        user.setFirst_name(dto.getFirst_name());
        user.setLast_name(dto.getLast_name());
        user.setContact_number(dto.getContact_number());

        user = userRepository.save(user);
        entityManager.refresh(user);
        return user;
    }
}

