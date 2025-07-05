package com.sss.app.service;

import com.sss.app.entity.UserAccount;
import com.sss.app.jwtToken.KeyProvider;
import com.sss.app.repository.UserAccountRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private KeyProvider keyProvider;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String authenticateAndGenerateToken(String username, String password) throws Exception {
        Optional<UserAccount> userCredentials = userAccountRepository.findByUserName(username);
        if (userCredentials.isPresent()) {
            UserAccount userAccount = userCredentials.get();

            if (userAccount.getUserName().equals(username) &&
                    passwordEncoder.matches(password, userAccount.getPasswordHash())) {

                return Jwts.builder()
                        .setSubject(username)
                        .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .setExpiration(Timestamp.valueOf(LocalDateTime.now().plusHours(12)))
                        .signWith(SignatureAlgorithm.RS256, keyProvider.getPrivateKey())
                        .compact();
            } else {
                throw new RuntimeException("Invalid credentials");
            }
        } else {
            throw new RuntimeException("User not found");
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
