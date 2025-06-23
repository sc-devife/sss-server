package com.sss.app.repository;

import com.sss.app.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<Login, String> {
    Optional<Login> findByUsername(String username);
    Optional<Login> findByEmail(String email);
    Optional<Login> findByResetToken(String resetToken);


}
