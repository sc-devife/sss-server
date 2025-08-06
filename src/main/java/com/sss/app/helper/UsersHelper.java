package com.sss.app.helper;

import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.entity.UserCredential;
import com.sss.app.entity.roles.Role;
import com.sss.app.entity.userrolelinks.UserRoleLink;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.RoleRepository;
import com.sss.app.repository.UserCredentialRepository;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.UserRoleLinkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UsersHelper {

    private final UserRepository userRepository;

    private final UserCredentialRepository userCredentialRepository;

    private final RoleRepository roleRepository;

    private final UserRoleLinkRepository userRoleLinkRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PersistenceContext
    private final EntityManager entityManager;
    /*public UsersHelper(UserRepository userRepository, UserCredentialRepository userCredentialRepository,
                       RoleRepository roleRepository,   UserRoleLinkRepository userRoleLinkRepository) {
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.roleRepository = roleRepository;
        this.userRoleLinkRepository = userRoleLinkRepository;
    }*/


    public User getUserByUid(String uid) {
        return userRepository.findUserWithRoles(uid).orElseThrow(() -> new NotFoundException("User not found with uid: " + uid));
    }

    public User getUserByEmail(String email) {
        System.out.println("UserHelper getUserByEmail Start === ");
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User createUser(UserCreateRequestDto payload) {
        System.out.println("Creating user with payload: " + payload);
        if (userRepository.existsByEmail(payload.getEmail())) {
            throw new ConflictException("Email is already in use");
        }

        User user = User.create(payload);
        user = userRepository.save(user);
        entityManager.refresh(user);

       // userCredential.setPassword_hash(passwordEncoder.encode(payload.getPassword()));
        UserCredential userCredential = UserCredential.create(user.getSeqp(), passwordEncoder.encode(payload.getPassword()));
        userCredentialRepository.save(userCredential);
        entityManager.refresh(userCredential);

        if (CollectionUtils.isEmpty(payload.getRoles())) {
            throw new IllegalArgumentException("No roles provided");
        }

        List<Role> roles = roleRepository.findByNameIn(payload.getRoles());

        if (roles.size() != payload.getRoles().size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }

        for (Role role : roles) {
            UserRoleLink userRoleLink = UserRoleLink.create(user, role);
            userRoleLinkRepository.save(userRoleLink);
        }

        entityManager.refresh(user);
        return user;
    }

    @Transactional
    public User updateUser(String uid, UserUpdateRequestDto payload) {
        User user = getUserByUid(uid);
        System.out.println("updateUser user - "+ user);
        user.update(payload);
        System.out.println("updateUser updated user - "+ user);
        userRepository.save(user);

        entityManager.refresh(user);
        return user;
    }

    @Transactional
    public User reassignRoles(String uid, List<String> roles) {
        User user = getUserByUid(uid);

        if (CollectionUtils.isEmpty(roles)) {
            throw new IllegalArgumentException("No roles provided");
        }

        List<UserRoleLink> currentRoleLinks = user.getRoles();

        List<Role> rolesList = roleRepository.findByNameIn(roles);

        if (rolesList.size() != roles.size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }

        List<UserRoleLink> roleLinksToDelete = currentRoleLinks.stream()
                .filter(link -> rolesList.stream()
                        .noneMatch(role -> role.getSeqp().equals(link.getRole().getSeqp())))
                .toList();

        List<Role> rolesToAdd = rolesList.stream()
                .filter(role -> currentRoleLinks.stream()
                        .noneMatch(link -> link.getRole().getSeqp().equals(role.getSeqp())))
                .toList();

        userRoleLinkRepository.deleteAll(roleLinksToDelete);

        List<UserRoleLink> linksToAdd = rolesToAdd.stream()
                .map(role -> UserRoleLink.create(user, role))
                .toList();
        userRoleLinkRepository.saveAll(linksToAdd);

        entityManager.refresh(user);
        return user;
    }
}

