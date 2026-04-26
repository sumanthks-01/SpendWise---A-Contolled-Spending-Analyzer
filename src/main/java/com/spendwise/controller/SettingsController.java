package com.spendwise.controller;

import com.spendwise.model.User;
import com.spendwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Settings are now stored on the User entity (homeCurrency, budgetLimit).
 * GET /api/settings  → returns current user's settings
 * POST /api/settings → updates a specific key (home_currency or budget_limit)
 */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired private UserService userService;

    @GetMapping
    public ResponseEntity<?> getSettings(Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(Map.of(
                "home_currency", user.getHomeCurrency(),
                "budget_limit",  user.getBudgetLimit().toString()
        ));
    }

    @PostMapping
    public ResponseEntity<?> saveSetting(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        User user = userService.findByEmail(auth.getName());
        String key   = body.get("key");
        String value = body.get("value");

        if (key == null || value == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "key and value required"));
        }

        try {
            switch (key) {
                case "home_currency" -> userService.updateHomeCurrency(user, value.toUpperCase());
                case "budget_limit"  -> userService.updateBudgetLimit(user, Double.parseDouble(value));
                default -> { return ResponseEntity.badRequest().body(Map.of("error", "Unknown setting: " + key)); }
            }
            return ResponseEntity.ok(Map.of("message", "Saved"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid number for budget_limit"));
        }
    }
}
