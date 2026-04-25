package com.spendwise.controller;

import com.spendwise.model.Subscription;
import com.spendwise.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    /** GET all subscriptions with computed daysUntilRenewal */
    @GetMapping
    public List<Subscription> getAll() {
        return service.getAllSubscriptions();
    }

    /** POST add a new subscription */
    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody Subscription sub) {
        if (sub.getName() == null || sub.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        if (sub.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than 0"));
        }
        service.addSubscription(sub);
        return ResponseEntity.ok(Map.of("success", true, "message", "Subscription added"));
    }

    /** DELETE a subscription by id */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable int id) {
        service.removeSubscription(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** GET total monthly spend in home currency */
    @GetMapping("/total")
    public Map<String, Object> getTotal(@RequestParam(defaultValue = "") String homeCurrency) {
        String home = homeCurrency.isBlank() ? service.getHomeCurrency() : homeCurrency;
        double total = service.calculateTotal(home);
        return Map.of(
                "total", Math.round(total * 100.0) / 100.0,
                "currency", home,
                "overBudget", service.isOverBudget(home),
                "budgetLimit", service.getBudgetLimit()
        );
    }

    /** GET spending by category (for pie chart) */
    @GetMapping("/chart")
    public Map<String, Double> getChart(@RequestParam(defaultValue = "") String homeCurrency) {
        String home = homeCurrency.isBlank() ? service.getHomeCurrency() : homeCurrency;
        return service.getChartData(home);
    }

    /** GET subscriptions renewing within 7 days */
    @GetMapping("/upcoming")
    public List<Subscription> getUpcoming() {
        return service.getUpcomingRenewals();
    }
}
