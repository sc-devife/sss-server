package com.sss.app.service;

import com.sss.app.dto.auth.LoginResponse;
import com.sss.app.entity.UserCredential;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.users.User;
import com.sss.app.exception.AccountBlockedException;
import com.sss.app.helper.UserCredentialsHelper;
import com.sss.app.helper.UsersHelper;
import com.sss.app.jwtToken.KeyProvider;
import com.sss.app.repository.OrganizationRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private final UsersHelper usersHelper;

    @Autowired
    private final UserCredentialsHelper userCredentialsHelper;

    @Autowired
    private final OrganizationRepository organizationRepository;

    @Autowired
    private KeyProvider keyProvider;

    public AuthenticationService(BCryptPasswordEncoder passwordEncoder, UsersHelper usersHelper, UserCredentialsHelper userCredentialsHelper, OrganizationRepository organizationRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usersHelper = usersHelper;
        this.userCredentialsHelper = userCredentialsHelper;
        this.organizationRepository = organizationRepository;
    }

    public LoginResponse authenticateAndGenerateToken(String email, String password) throws Exception {
        User user = usersHelper.getUserByEmail(email);

        UserCredential userCredential = userCredentialsHelper.getUserCredentialBySeqa(user.getSeqp());

        if (user.getEmail().equals(email) &&
                passwordEncoder.matches(password, userCredential.getPassword_hash())) {

            if (Boolean.TRUE.equals(user.getBlocked())) {
                throw new AccountBlockedException("Your account has been blocked. Please contact your organization administrator.");
            }

            String token = Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .setExpiration(Timestamp.valueOf(LocalDateTime.now().plusHours(12)))
                    .signWith(SignatureAlgorithm.RS256, keyProvider.getPrivateKey())
                    .compact();

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
        } else {
            throw new RuntimeException("Invalid credentials");
        }
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
