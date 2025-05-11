// src/main/java/com/mondo/erp/core/repository/PasswordResetTokenRepository.java
package com.mondo.erp.core.repository;

import com.mondo.erp.core.model.PasswordResetToken;
import com.mondo.erp.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByUser(User user);
}