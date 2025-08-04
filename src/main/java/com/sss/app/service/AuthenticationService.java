package com.sss.app.service;

import com.sss.app.entity.UserCredential;
import com.sss.app.entity.users.User;
import com.sss.app.helper.UserCredentialsHelper;
import com.sss.app.helper.UsersHelper;
import com.sss.app.jwtToken.KeyProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class AuthenticationService {

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private final UsersHelper usersHelper;

    @Autowired
    private final UserCredentialsHelper userCredentialsHelper;

    @Autowired
    private KeyProvider keyProvider;

    public AuthenticationService(BCryptPasswordEncoder passwordEncoder, UsersHelper usersHelper, UserCredentialsHelper userCredentialsHelper) {
        this.passwordEncoder = passwordEncoder;
        this.usersHelper = usersHelper;
        this.userCredentialsHelper = userCredentialsHelper;
    }

    public String authenticateAndGenerateToken(String email, String password) throws Exception {
        User user = usersHelper.getUserByEmail(email);
        UserCredential userCredential = userCredentialsHelper.getUserCredentialBySeqa(user.getSeqp());

        if (user.getEmail().equals(email) &&
                passwordEncoder.matches(password, userCredential.getPassword_hash())) {

            return Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .setExpiration(Timestamp.valueOf(LocalDateTime.now().plusHours(12)))
                    .signWith(SignatureAlgorithm.RS256, keyProvider.getPrivateKey())
                    .compact();
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
}
