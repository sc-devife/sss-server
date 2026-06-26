package com.sss.app.repository.signup;

import com.sss.app.entity.signup.Signup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignupRepository extends JpaRepository<Signup, Long> {

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);

    Optional<Signup> findFirstByEmailOrMobileNumberOrderByCreatedAtDesc(String email, String mobileNumber);
}
