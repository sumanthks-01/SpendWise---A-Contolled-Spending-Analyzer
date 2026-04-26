package com.spendwise.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * JPA entity for a user's subscription entry.
 */
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency = "INR";

    private String category = "Others";

    @Column(name = "renewal_date")
    private String renewalDate;

    @Column(name = "billing_cycle")
    private String billingCycle = "Monthly";

    // ── Constructors ──────────────────────────────────────────────────────────

    public Subscription() {}

    // ── Computed / Transient fields (serialised to JSON) ──────────────────────

    /** Monthly cost in the subscription's own currency (no conversion). */
    @Transient
    public Double getMonthlyEquivalent() {
        if (amount == null) return 0.0;
        if ("Yearly".equalsIgnoreCase(billingCycle))  return amount / 12.0;
        if ("Weekly".equalsIgnoreCase(billingCycle))  return amount * 4.333;
        return amount;
    }

    /** Days until next renewal. -999 = no date set. Negative = overdue. */
    @Transient
    public long getDaysUntilRenewal() {
        if (renewalDate == null || renewalDate.isBlank()) return -999;
        try {
            return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(renewalDate));
        } catch (Exception e) {
            return -999;
        }
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getRenewalDate() { return renewalDate; }
    public void setRenewalDate(String renewalDate) { this.renewalDate = renewalDate; }
    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }
}
