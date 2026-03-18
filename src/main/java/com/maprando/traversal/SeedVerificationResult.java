package com.maprando.traversal;

import java.util.Set;

/**
 * Result of seed verification analysis.
 */
public class SeedVerificationResult {

    public enum VerificationStatus {
        BEATABLE,
        UNBEATABLE,
        SOFT_LOCKED,
        INCOMPLETE
    }

    private final VerificationStatus status;
    private final boolean beatable;
    private final Set<String> unreachableLocations;
    private final Set<String> criticalPathItems;
    private final boolean hasSoftLocks;
    private final boolean hasImpossibleRequirements;
    private final String message;

    public SeedVerificationResult(VerificationStatus status, boolean beatable,
                                 Set<String> unreachableLocations,
                                 Set<String> criticalPathItems,
                                 boolean hasSoftLocks,
                                 boolean hasImpossibleRequirements,
                                 String message) {
        this.status = status;
        this.beatable = beatable;
        this.unreachableLocations = unreachableLocations;
        this.criticalPathItems = criticalPathItems;
        this.hasSoftLocks = hasSoftLocks;
        this.hasImpossibleRequirements = hasImpossibleRequirements;
        this.message = message;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public boolean isBeatable() {
        return beatable;
    }

    public Set<String> getUnreachableLocations() {
        return unreachableLocations;
    }

    public Set<String> getCriticalPathItems() {
        return criticalPathItems;
    }

    public boolean hasSoftLocks() {
        return hasSoftLocks;
    }

    public boolean hasImpossibleRequirements() {
        return hasImpossibleRequirements;
    }

    public String getMessage() {
        return message;
    }

    public boolean areProgressionItemsAccessible() {
        return beatable && !hasImpossibleRequirements;
    }

    @Override
    public String toString() {
        return "SeedVerificationResult{" +
                "status=" + status +
                ", beatable=" + beatable +
                ", unreachableCount=" + unreachableLocations.size() +
                '}';
    }
}