package com.spendwise.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Subscription {
    private int id;
    private String name;
    private double amount;
    private String currency;      // e.g. "INR", "USD", "EUR"
    private String category;
    private String renewalDate;   // ISO-8601: YYYY-MM-DD
    private String billingCycle;  // Monthly | Weekly | Yearly

    // ── Constructors ─────────────────────────────────────────────────────────

    public Subscription() {}

    public Subscription(String name, double amount, String currency,
                        String category, String renewalDate, String billingCycle) {
        this.name = name;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.renewalDate = renewalDate;
        this.billingCycle = billingCycle;
    }

    public Subscription(int id, String name, double amount, String currency,
                        String category, String renewalDate, String billingCycle) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.renewalDate = renewalDate;
        this.billingCycle = billingCycle;
    }

    // ── Computed: monthly equivalent amount (before currency conversion) ──────

    public double getMonthlyEquivalent() {
        if (billingCycle == null) return amount;
        return switch (billingCycle) {
            case "Yearly"  -> amount / 12.0;
            case "Weekly"  -> amount * 4.33;
            default        -> amount;           // Monthly
        };
    }

    // ── Computed: days until next renewal ────────────────────────────────────

    public long getDaysUntilRenewal() {
        if (renewalDate == null || renewalDate.isEmpty()) return -999; // sentinel: no date set
        try {
            return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(renewalDate));
        } catch (Exception e) {
            return -999;
        }
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getName()                   { return name; }
    public void setName(String name)          { this.name = name; }

    public double getAmount()                 { return amount; }
    public void setAmount(double amount)      { this.amount = amount; }

    public String getCurrency()               { return currency != null ? currency : "INR"; }
    public void setCurrency(String currency)  { this.currency = currency; }

    public String getCategory()               { return category; }
    public void setCategory(String category)  { this.category = category; }

    public String getRenewalDate()            { return renewalDate; }
    public void setRenewalDate(String d)      { this.renewalDate = d; }

    public String getBillingCycle()           { return billingCycle; }
    public void setBillingCycle(String bc)    { this.billingCycle = bc; }
}
