// src/main/java/com/mondo/erp/application/config/DevMailConfig.java
package com.mondo.erp.application.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@Profile("dev")
public class DevMailConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevMailConfig.class);

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Log mail settings instead of actually sending
        mailSender.setHost("localhost");
        mailSender.setPort(25);

        // Override the send method to log instead of actually sending
        return new JavaMailSenderImpl() {
            @Override
            public void send(org.springframework.mail.SimpleMailMessage simpleMessage) {
                logger.info("Sending mail: ");
                logger.info("From: {}", simpleMessage.getFrom());
                logger.info("To: {}", String.join(", ", simpleMessage.getTo()));
                logger.info("Subject: {}", simpleMessage.getSubject());
                logger.info("Text: {}", simpleMessage.getText());
            }
        };
    }
}