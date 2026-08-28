package com.sss.app.service;

import com.sss.app.dto.auth.LoginResponse;
import com.sss.app.entity.UserCredential;
import com.sss.app.entity.UserSession;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.users.User;
import com.sss.app.exception.AccountBlockedException;
import com.sss.app.exception.AccountLockedException;
import com.sss.app.helper.UserCredentialsHelper;
import com.sss.app.helper.UsersHelper;
import com.sss.app.jwtToken.KeyProvider;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.UserSessionRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);
    private static final DateTimeFormatter LOCKOUT_MESSAGE_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private final UsersHelper usersHelper;

    @Autowired
    private final UserCredentialsHelper userCredentialsHelper;

    @Autowired
    private final OrganizationRepository organizationRepository;

    @Autowired
    private final UserSessionRepository userSessionRepository;

    @Autowired
    private KeyProvider keyProvider;

    public AuthenticationService(BCryptPasswordEncoder passwordEncoder, UsersHelper usersHelper, UserCredentialsHelper userCredentialsHelper,
                                  OrganizationRepository organizationRepository, UserSessionRepository userSessionRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usersHelper = usersHelper;
        this.userCredentialsHelper = userCredentialsHelper;
        this.organizationRepository = organizationRepository;
        this.userSessionRepository = userSessionRepository;
    }

    public LoginResponse authenticateAndGenerateToken(String email, String password, String deviceInfo, String ipAddress) throws Exception {
        User user = usersHelper.getUserByEmail(email);
        UserCredential userCredential = userCredentialsHelper.getUserCredentialByUserSeqp(user.getSeqp());

        if (userCredential.isLocked()) {
            throw new AccountLockedException("Too many failed login attempts. Try again after "
                    + userCredential.getLockedUntil().format(LOCKOUT_MESSAGE_TIME_FORMAT) + ".");
        }

        if (!(user.getEmail().equals(email) && passwordEncoder.matches(password, userCredential.getPassword_hash()))) {
            userCredential.registerFailedAttempt(MAX_FAILED_LOGIN_ATTEMPTS, LOCKOUT_DURATION);
            userCredentialsHelper.save(userCredential);
            throw new RuntimeException("Invalid credentials");
        }

        userCredential.registerSuccessfulLogin();
        userCredentialsHelper.save(userCredential);

        if (Boolean.TRUE.equals(user.getBlocked())) {
            throw new AccountBlockedException("Your account has been blocked. Please contact your organization administrator.");
        }

        UUID sessionId = UUID.randomUUID();
        String token = Jwts.builder()
                .setSubject(email)
                .claim("sessionId", sessionId.toString())
                .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .setExpiration(Timestamp.valueOf(LocalDateTime.now().plusHours(12)))
                .signWith(SignatureAlgorithm.RS256, keyProvider.getPrivateKey())
                .compact();

        userSessionRepository.save(UserSession.create(sessionId, user, token, deviceInfo, ipAddress));

        String role = user.getRoles().stream()
                .map(link -> link.getRole().getName())
                .collect(Collectors.joining(","));

        // Joined through the user's existing orgId relationship, not
        // duplicated onto the users table — same pattern as
        // UsersServiceImpl.toProfileResponseDto for the self-service
        // profile endpoints.
        String organizationLogo = user.getOrgId() == null ? null
                : organizationRepository.findById(user.getOrgId())
                        .map(Organizations::getLogoFile)
                        .orElse(null);

        return new LoginResponse(token, user.getUserId(), user.getName(), role, organizationLogo);
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(keyProvider.getPrivateKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(keyProvider.getPrivateKey()).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String generatePasswordHash(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
}
