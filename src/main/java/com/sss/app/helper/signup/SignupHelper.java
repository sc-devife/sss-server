package com.sss.app.helper.signup;

import com.sss.app.dto.signup.SignupCreateRequestDTO;
import com.sss.app.entity.UserCredential;
import com.sss.app.entity.roles.Role;
import com.sss.app.entity.userrolelinks.UserRoleLink;
import com.sss.app.entity.users.User;
import com.sss.app.entity.users.invitations.UserInvitation;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.signup.SignupMapper;
import com.sss.app.repository.InvitationTokenRepository;
import com.sss.app.repository.RoleRepository;
import com.sss.app.repository.UserCredentialRepository;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.UserRoleLinkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SignupHelper {

    private static final long SIGNUP_RETRY_LIMIT_HOURS = 24;

    private final SignupMapper signupMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final RoleRepository roleRepository;
    private final UserRoleLinkRepository userRoleLinkRepository;

    @Transactional
    public User createSignup(SignupCreateRequestDTO payload) {
        UserInvitation invitation = invitationTokenRepository.findByUid(payload.getInvitationToken())
                .orElseThrow(() -> new NotFoundException("Invalid invitation"));

        if (invitation.is_used()) {
            throw new ConflictException("This invitation has already been used");
        }
        if (invitation.is_archived()) {
            throw new ConflictException("This invitation is no longer valid");
        }
        if (invitation.getExpires_set().isBefore(LocalDateTime.now())) {
            throw new ConflictException("This invitation has expired");
        }
        if (!invitation.getEmail().equalsIgnoreCase(payload.getEmail())) {
            throw new ConflictException("This invitation was issued for a different email address");
        }

        if (userRepository.existsByUserId(payload.getUserId())) {
            throw new ConflictException("User ID is already registered");
        }

        if (userRepository.existsByEmail(payload.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        List<User> previousSignups = userRepository.findByEmailOrContactNumberOrderByCreatedAtDesc(
                payload.getEmail(), payload.getMobileNumber());

        if (!previousSignups.isEmpty()
                && previousSignups.get(0).getCreatedAt() != null
                && previousSignups.get(0).getCreatedAt().isAfter(LocalDateTime.now().minusHours(SIGNUP_RETRY_LIMIT_HOURS))) {
            throw new ConflictException("A signup with this email or mobile number was already attempted in the last 24 hours");
        }

        String passwordHash = passwordEncoder.encode(payload.getPassword());

        User user = signupMapper.toEntity(payload);
        user.setCreatedAt(LocalDateTime.now());
        user.setOrgId(invitation.getOrgId());
        user.setInvitedBy(invitation.getInvitedBy());
        user = userRepository.save(user);

        UserCredential userCredential = UserCredential.create(user, passwordHash);
        userCredentialRepository.save(userCredential);

        if (invitation.getRoles() != null && !invitation.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findByNameIn(invitation.getRoles());
            for (Role role : roles) {
                userRoleLinkRepository.save(UserRoleLink.create(user, role));
            }
        }

        invitation.set_used(true);
        invitationTokenRepository.save(invitation);

        return user;
    }
}
