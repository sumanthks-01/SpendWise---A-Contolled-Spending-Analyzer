package com.spendwise.service;

import com.spendwise.model.Subscription;
import com.spendwise.repository.DatabaseHandler;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final DatabaseHandler db;
    private final CurrencyService currencyService;

    public SubscriptionService(DatabaseHandler db, CurrencyService currencyService) {
        this.db = db;
        this.currencyService = currencyService;
        this.db.initializeDatabase();
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public void addSubscription(Subscription sub)  { db.addSubscription(sub); }
    public void removeSubscription(int id)          { db.deleteSubscription(id); }
    public List<Subscription> getAllSubscriptions() { return db.getAllSubscriptions(); }

    // ── Settings ──────────────────────────────────────────────────────────────

    public double getBudgetLimit() {
        String v = db.getSetting("budget_limit");
        try { return v != null ? Double.parseDouble(v) : 5000.0; }
        catch (NumberFormatException e) { return 5000.0; }
    }

    public void setBudgetLimit(double limit) {
        db.saveSetting("budget_limit", String.valueOf(limit));
    }

    public String getHomeCurrency() {
        String v = db.getSetting("home_currency");
        return v != null ? v : "INR";
    }

    public void setHomeCurrency(String currency) {
        db.saveSetting("home_currency", currency.toUpperCase());
    }

    public Map<String, String> getAllSettings() {
        return db.getAllSettings();
    }

    // ── Calculations ──────────────────────────────────────────────────────────

    /**
     * Total monthly spend in home currency.
     * Each subscription's monthly equivalent is converted from its own currency.
     */
    public double calculateTotal(String homeCurrency) {
        String home = homeCurrency != null ? homeCurrency.toUpperCase() : getHomeCurrency();
        return getAllSubscriptions().stream()
                .mapToDouble(sub -> {
                    double monthly = sub.getMonthlyEquivalent();
                    return currencyService.convert(monthly, sub.getCurrency(), home);
                })
                .sum();
    }

    public boolean isOverBudget(String homeCurrency) {
        return calculateTotal(homeCurrency) > getBudgetLimit();
    }

    /**
     * Spending by category in home currency (for pie chart).
     */
    public Map<String, Double> getChartData(String homeCurrency) {
        String home = homeCurrency != null ? homeCurrency.toUpperCase() : getHomeCurrency();
        Map<String, Double> data = new LinkedHashMap<>();
        for (Subscription sub : getAllSubscriptions()) {
            double monthly = sub.getMonthlyEquivalent();
            double converted = currencyService.convert(monthly, sub.getCurrency(), home);
            data.merge(sub.getCategory() != null ? sub.getCategory() : "Others", converted, Double::sum);
        }
        return data;
    }

    /**
     * Subscriptions renewing within the next 7 days.
     */
    public List<Subscription> getUpcomingRenewals() {
        return getAllSubscriptions().stream()
                .filter(s -> s.getDaysUntilRenewal() >= 0 && s.getDaysUntilRenewal() <= 7)
                .collect(Collectors.toList());
    }
}
