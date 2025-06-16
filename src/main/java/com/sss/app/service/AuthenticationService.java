package com.sss.app.service;

import com.sss.app.entity.Login;
import com.sss.app.repository.LoginRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import com.sss.app.jwtToken.KeyProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private static KeyProvider keyProvider = new KeyProvider();

    public AuthenticationService(KeyProvider keyProvider) {
        AuthenticationService.keyProvider = keyProvider;
    }
    @Autowired
    private LoginRepository loginRepository;
    //KeyProvider keyProvider = new KeyProvider();
    public String authenticateAndGenerateToken(String username, String password) throws Exception {
        Optional<Login> userCredentials = loginRepository.findByUsername(username);
        if (userCredentials.isPresent()) {
            Login login = userCredentials.get();

            if (login.getUsername().equals(username) && login.getPassword().equals(password)) {
                return Jwts.builder()
                        .setSubject(username)
                        .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .setExpiration(Timestamp.valueOf(LocalDateTime.now().plusHours(12))) // 12 hours expiration
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

    public static boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(keyProvider.getPrivateKey()).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
