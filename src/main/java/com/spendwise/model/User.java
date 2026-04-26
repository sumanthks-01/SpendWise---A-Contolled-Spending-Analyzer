package com.spendwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered SpendWise user.
 * Settings (home currency, budget) live here to avoid a separate settings table.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    /** BCrypt-hashed password. Null = social-login-only account (future). */
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "home_currency", nullable = false)
    private String homeCurrency = "INR";

    @Column(name = "budget_limit", nullable = false)
    private Double budgetLimit = 5000.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subscription> subscriptions = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getHomeCurrency() { return homeCurrency; }
    public void setHomeCurrency(String homeCurrency) { this.homeCurrency = homeCurrency; }
    public Double getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(Double budgetLimit) { this.budgetLimit = budgetLimit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<Subscription> getSubscriptions() { return subscriptions; }
}
