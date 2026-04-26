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
}
