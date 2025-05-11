// src/main/java/com/mondo/erp/core/service/impl/PasswordResetServiceImpl.java
package com.mondo.erp.core.service.impl;

import com.mondo.erp.core.model.PasswordResetToken;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.repository.PasswordResetTokenRepository;
import com.mondo.erp.core.service.PasswordResetService;
import com.mondo.erp.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserService userService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@mondoerp.com}")
    private String fromEmail;

    @Autowired
    public PasswordResetServiceImpl(
            PasswordResetTokenRepository tokenRepository,
            UserService userService,
            JavaMailSender mailSender) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.mailSender = mailSender;
    }

    @Override
    @Transactional
    public PasswordResetToken createToken(User user) {
        // Delete any existing tokens for the user
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        // Create new token
        PasswordResetToken token = new PasswordResetToken(user);
        return tokenRepository.save(token);
    }

    @Override
    public Optional<PasswordResetToken> getValidToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        // Check if token exists and is not expired
        if (tokenOpt.isPresent() && !tokenOpt.get().isExpired()) {
            return tokenOpt;
        }

        // Delete token if expired
        tokenOpt.ifPresent(tokenRepository::delete);
        return Optional.empty();
    }

    @Override
    @Transactional
    public void deleteToken(PasswordResetToken token) {
        tokenRepository.delete(token);
    }

    @Override
    public boolean sendPasswordResetEmail(User user, String resetUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Mondo ERP - Password Reset");
            message.setText("Hi " + user.getFirstname() + ",\n\n" +
                    "You have requested to reset your password. Please click the link below to set a new password:\n\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "Regards,\nThe Mondo ERP Team");

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean resetPassword(User user, String newPassword) {
        try {
            user.setPassword(newPassword);
            userService.save(user);

            // Delete the token after successful password reset
            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}