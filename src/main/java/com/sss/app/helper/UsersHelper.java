package com.sss.app.helper;

import com.sss.app.dto.users.UserAssignmentSettingsUpdateRequestDto;
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
import com.sss.app.security.OrgAccessGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final OrgAccessGuard orgAccessGuard;

    @PersistenceContext
    private final EntityManager entityManager;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /** Users in the caller's own organization (Super Admins see the org they pass explicitly — not wired here yet). */
    public List<User> fetchAllUsers(Long ignoredLegacyParam) {
        return userRepository.findUsersWithRoles(currentUser().getOrgId());
    }

    public User getUserByUid(String uid) {
        User user = userRepository.findUserWithRoles(uid).orElseThrow(() -> new NotFoundException("User not found with uid: " + uid));
        orgAccessGuard.requireAccessToOrg(user.getOrgId());
        return user;
    }

    // Eager-fetches roles (rather than plain findByEmail) since login needs
    // them to compute LoginResponse.role without relying on open-in-view to
    // keep a lazy proxy resolvable later in the request.
    public User getUserByEmail(String email) {
        return userRepository.findByEmailWithRoles(email).orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User createUser(UserCreateRequestDto payload) {
        if (userRepository.existsByEmail(payload.getEmail())) {
            throw new ConflictException("Email is already in use");
        }

        User user = User.create(payload);
        user.setOrgId(currentUser().getOrgId());
        user.setInvitedBy(currentUser().getSeqp());
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
        user.update(payload);
        userRepository.save(user);

        // See reassignRoles() — entityManager.refresh(user) cascades onto
        // the cascade=ALL roles collection, which is fragile (it's what
        // caused that method's "instance was not in a valid state" crash
        // and silent re-insert bug). Clearing the persistence context and
        // re-fetching avoids that risk entirely, even though this method
        // doesn't touch roles itself.
        entityManager.flush();
        entityManager.clear();
        return getUserByUid(uid);
    }

    @Transactional
    public User updateAssignmentSettings(String uid, UserAssignmentSettingsUpdateRequestDto payload) {
        User user = getUserByUid(uid);
        if (payload.getIsSpecialist() != null) {
            user.setIsSpecialist(payload.getIsSpecialist());
        }
        if (payload.getSpecialistEscapePoints() != null) {
            user.setSpecialistEscapePoints(payload.getSpecialistEscapePoints());
        }
        if (payload.getMaxConcurrentAssignments() != null) {
            user.setMaxConcurrentAssignments(payload.getMaxConcurrentAssignments());
        }
        if (payload.getEligibleForPriorityLeads() != null) {
            user.setEligibleForPriorityLeads(payload.getEligibleForPriorityLeads());
        }
        if (payload.getAcceptingLeads() != null) {
            user.setAcceptingLeads(payload.getAcceptingLeads());
        }
        userRepository.save(user);

        entityManager.flush();
        entityManager.refresh(user);
        return user;
    }

    @Transactional
    public User setBlocked(String uid, boolean blocked) {
        User user = getUserByUid(uid);
        user.setBlocked(blocked);
        userRepository.save(user);

        // See reassignRoles() — entityManager.refresh(user) cascades onto
        // the cascade=ALL roles collection, which is fragile (it's what
        // caused that method's "instance was not in a valid state" crash
        // and silent re-insert bug). Clearing the persistence context and
        // re-fetching avoids that risk entirely, even though this method
        // doesn't touch roles itself.
        entityManager.flush();
        entityManager.clear();
        return getUserByUid(uid);
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

        // user.getRoles() is the live, cascade=ALL-managed collection (not a
        // copy). If we delete roleLinksToDelete through the repository
        // without also removing them from this collection, Hibernate still
        // sees them as reachable via a cascade=PERSIST association at flush
        // time and re-inserts the very rows we just told it to delete.
        currentRoleLinks.removeAll(roleLinksToDelete);
        userRoleLinkRepository.deleteAll(roleLinksToDelete);

        List<UserRoleLink> linksToAdd = rolesToAdd.stream()
                .map(role -> UserRoleLink.create(user, role))
                .toList();
        currentRoleLinks.addAll(linksToAdd);
        userRoleLinkRepository.saveAll(linksToAdd);

        // entityManager.refresh(user) used to be called here to pick up the
        // change, but cascading REFRESH onto this collection throws
        // "instance was not in a valid state" once it contains entities that
        // were just deleted. Clearing the persistence context and
        // re-fetching through the same JOIN FETCH query sidesteps
        // refresh-cascade entirely and guarantees the returned user reflects
        // exactly what's committed.
        entityManager.flush();
        entityManager.clear();
        return getUserByUid(uid);
    }
}

