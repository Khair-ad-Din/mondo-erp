// src/main/java/com/mondo/erp/core/service/PasswordResetService.java
package com.mondo.erp.core.service;

import com.mondo.erp.core.model.PasswordResetToken;
import com.mondo.erp.core.model.User;

import java.util.Optional;

public interface PasswordResetService {

    PasswordResetToken createToken(User user);

    Optional<PasswordResetToken> getValidToken(String token);

    void deleteToken(PasswordResetToken token);

    boolean sendPasswordResetEmail(User user, String resetUrl);

    boolean resetPassword(User user, String newPassword);
}