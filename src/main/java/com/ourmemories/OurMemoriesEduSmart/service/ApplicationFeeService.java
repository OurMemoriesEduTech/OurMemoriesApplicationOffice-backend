package com.ourmemories.OurMemoriesEduSmart.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApplicationFeeService {

    private static final Map<Integer, BigDecimal> FEE_STRUCTURE = new HashMap<>();

    static {
        FEE_STRUCTURE.put(1, new BigDecimal("70.00"));
        FEE_STRUCTURE.put(2, new BigDecimal("130.00"));
        FEE_STRUCTURE.put(3, new BigDecimal("190.00"));
        FEE_STRUCTURE.put(4, new BigDecimal("250.00"));
        FEE_STRUCTURE.put(5, new BigDecimal("310.00"));
    }

    /**
     * Calculate fee based on number of applications
     */
    public BigDecimal calculateFee(int numberOfApplications) {
        if (numberOfApplications < 1 || numberOfApplications > 5) {
            throw new IllegalArgumentException("Number of applications must be between 1 and 5");
        }
        return FEE_STRUCTURE.get(numberOfApplications);
    }

    /**
     * Check if user qualifies for free NSFAS application
     */
    public boolean qualifiesForFreeNSFAS(int numberOfApplications) {
        return numberOfApplications >= 5;
    }

    /**
     * Get fee description
     */
    public String getFeeDescription(int numberOfApplications) {
        String description = numberOfApplications + " application";
        if (numberOfApplications > 1) {
            description += "s";
        }
        if (qualifiesForFreeNSFAS(numberOfApplications)) {
            description += " (including FREE NSFAS application)";
        }
        return description;
    }

    /**
     * Get complete fee structure
     */
    public Map<String, BigDecimal> getFeeStructure() {
        Map<String, BigDecimal> structure = new HashMap<>();
        structure.put("1 Application", new BigDecimal("70.00"));
        structure.put("2 Applications", new BigDecimal("130.00"));
        structure.put("3 Applications", new BigDecimal("190.00"));
        structure.put("4 Applications", new BigDecimal("250.00"));
        structure.put("5 Applications", new BigDecimal("310.00"));
        structure.put("Free NSFAS", new BigDecimal("0.00"));
        return structure;
    }

    /**
     * Get fee breakdown
     */
    public Map<String, Object> getFeeBreakdown(int numberOfApplications) {
        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("totalApplications", numberOfApplications);
        breakdown.put("totalFee", calculateFee(numberOfApplications));
        breakdown.put("freeNSFASIncluded", qualifiesForFreeNSFAS(numberOfApplications));
        breakdown.put("perApplicationFee", new BigDecimal("70.00"));

        if (numberOfApplications >= 5) {
            breakdown.put("nsfasFee", new BigDecimal("0.00"));
            breakdown.put("applicationsFee", new BigDecimal("310.00"));
        } else {
            breakdown.put("applicationsFee", calculateFee(numberOfApplications));
        }

        return breakdown;
    }
}