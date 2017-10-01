package com.playback.models;

import java.util.Objects;

public class Customer {
    public enum Plan {
        NORMAL, PREMIUM
    }

    private final boolean active;
    private final Plan plan;

    private Customer(boolean active, Plan plan) {
        this.active = active;
        this.plan = plan;
    }

    public boolean isActive() {
        return active;
    }

    public Plan getPlan() {
        return plan;
    }

    public static Customer active(Plan plan) {
        return new Customer(true, plan);
    }

    public static Customer inactive(Plan plan) {
        return new Customer(false, plan);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return active == customer.active &&
                plan == customer.plan;
    }

    @Override
    public int hashCode() {
        return Objects.hash(active, plan);
    }
}
