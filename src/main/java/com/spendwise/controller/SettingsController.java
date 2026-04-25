package com.spendwise.controller;

import com.spendwise.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SubscriptionService service;

    public SettingsController(SubscriptionService service) {
        this.service = service;
    }

    /** GET all settings */
    @GetMapping
    public Map<String, String> getAll() {
        return service.getAllSettings();
    }

    /** POST save a setting key/value */
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody Map<String, String> body) {
        String key = body.get("key");
        String value = body.get("value");

        if (key == null || value == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "key and value are required"));
        }

        switch (key) {
            case "budget_limit" -> {
                try {
                    service.setBudgetLimit(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "budget_limit must be a number"));
                }
            }
            case "home_currency" -> service.setHomeCurrency(value);
            default -> {
                return ResponseEntity.badRequest().body(Map.of("error", "Unknown setting: " + key));
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "key", key, "value", value));
    }
}
