package com.spendwise.controller;

import com.spendwise.model.Subscription;
import com.spendwise.model.User;
import com.spendwise.service.SubscriptionService;
import com.spendwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired private SubscriptionService subscriptionService;
    @Autowired private UserService userService;

    /** Helper: resolve current user from Spring Security session. */
    private User currentUser(Authentication auth) {
        return userService.findByEmail(auth.getName());
    }

    @GetMapping
    public List<Subscription> getAll(Authentication auth) {
        return subscriptionService.getAllSubscriptions(currentUser(auth));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Subscription sub, Authentication auth) {
        try {
            Subscription saved = subscriptionService.addSubscription(currentUser(auth), sub);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        try {
            subscriptionService.deleteSubscription(id, currentUser(auth));
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/total")
    public Map<String, Object> total(Authentication auth) {
        return subscriptionService.getMonthlyTotal(currentUser(auth));
    }

    @GetMapping("/upcoming")
    public List<Subscription> upcoming(Authentication auth) {
        return subscriptionService.getUpcomingRenewals(currentUser(auth));
    }

    @GetMapping("/chart")
    public Map<String, Double> chart(Authentication auth) {
        return subscriptionService.getChartData(currentUser(auth));
    }
}
