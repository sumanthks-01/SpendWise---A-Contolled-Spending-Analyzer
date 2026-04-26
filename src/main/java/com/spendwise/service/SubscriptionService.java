package com.spendwise.service;

import com.spendwise.model.Subscription;
import com.spendwise.model.User;
import com.spendwise.repository.SubscriptionRepository;
import com.spendwise.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    @Autowired private SubscriptionRepository subscriptionRepo;
    @Autowired private CurrencyService currencyService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public List<Subscription> getAllSubscriptions(User user) {
        return subscriptionRepo.findByUser(user);
    }

    public Subscription addSubscription(User user, Subscription sub) {
        sub.setUser(user);
        return subscriptionRepo.save(sub);
    }

    @Transactional
    public void deleteSubscription(Long id, User user) {
        subscriptionRepo.deleteByIdAndUser(id, user);
    }

    // ── Dashboard data ────────────────────────────────────────────────────────

    /** Returns upcoming renewals (0–7 days) for this user. */
    public List<Subscription> getUpcomingRenewals(User user) {
        return getAllSubscriptions(user).stream()
                .filter(s -> s.getDaysUntilRenewal() >= 0 && s.getDaysUntilRenewal() <= 7)
                .collect(Collectors.toList());
    }

    /**
     * Returns total monthly spend converted to homeCurrency,
     * along with overBudget flag and the budget limit.
     */
    public Map<String, Object> getMonthlyTotal(User user) {
        String home = user.getHomeCurrency();
        double budget = user.getBudgetLimit();

        double total = getAllSubscriptions(user).stream()
                .mapToDouble(s -> {
                    double monthly = s.getMonthlyEquivalent();
                    if (!s.getCurrency().equalsIgnoreCase(home)) {
                        monthly = currencyService.convert(monthly, s.getCurrency(), home);
                    }
                    return monthly;
                })
                .sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", Math.round(total * 100.0) / 100.0);
        result.put("budgetLimit", budget);
        result.put("overBudget", total > budget);
        return result;
    }

    /**
     * Returns per-category spending totals (converted to homeCurrency) for the chart.
     */
    public Map<String, Double> getChartData(User user) {
        String home = user.getHomeCurrency();
        Map<String, Double> chart = new LinkedHashMap<>();

        getAllSubscriptions(user).forEach(s -> {
            double monthly = s.getMonthlyEquivalent();
            if (!s.getCurrency().equalsIgnoreCase(home)) {
                monthly = currencyService.convert(monthly, s.getCurrency(), home);
            }
            String cat = s.getCategory() != null ? s.getCategory() : "Others";
            chart.merge(cat, Math.round(monthly * 100.0) / 100.0, Double::sum);
        });

        return chart;
    }
}
