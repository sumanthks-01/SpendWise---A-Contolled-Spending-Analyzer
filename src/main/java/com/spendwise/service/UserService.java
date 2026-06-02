package com.spendwise.service;

import com.spendwise.model.User;
import com.spendwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * Registers a new user. Returns the saved User or throws if email already exists.
     */
    public User register(String name, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        String hash = passwordEncoder.encode(rawPassword);
        User user = new User(name, email, hash);
        return userRepository.save(user);
    }

    /** Loads user by email — used by controllers to get the full entity from the session email. */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    /** Updates home currency for the given user. */
    public void updateHomeCurrency(User user, String currency) {
        user.setHomeCurrency(currency);
        userRepository.save(user);
    }

    /** Updates budget limit for the given user. */
    public void updateBudgetLimit(User user, Double limit) {
        user.setBudgetLimit(limit);
        userRepository.save(user);
    }

    @Autowired private com.spendwise.repository.PasswordResetTokenRepository tokenRepository;

    @org.springframework.transaction.annotation.Transactional
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with that email."));

        // Clean up any old tokens for this user
        tokenRepository.deleteByUser(user);

        String token = java.util.UUID.randomUUID().toString();
        com.spendwise.model.PasswordResetToken myToken = new com.spendwise.model.PasswordResetToken(
                token, user, java.time.LocalDateTime.now().plusMinutes(15)
        );
        tokenRepository.save(myToken);
        return token;
    }

    @org.springframework.transaction.annotation.Transactional
    public void resetPassword(String token, String newPassword) {
        com.spendwise.model.PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token."));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("This reset link has already been used.");
        }
        if (resetToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
