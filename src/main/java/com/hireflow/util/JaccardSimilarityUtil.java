package com.hireflow.util;

import java.util.Set;

public class JaccardSimilarityUtil {

    /**
     * Computes Jaccard similarity: |A ∩ B| / |A ∪ B|
     * Returns a score between 0.0 (no overlap) and 1.0 (perfect match).
     */
    public static double compute(Set<String> setA, Set<String> setB) {
        if (setA.isEmpty() && setB.isEmpty()) {
            return 1.0; // Both empty → perfect match
        }
        if (setA.isEmpty() || setB.isEmpty()) {
            return 0.0;
        }

        long intersection = setA.stream()
            .filter(setB::contains)
            .count();

        long union = setA.size() + setB.size() - intersection;

        return union == 0 ? 0.0 : (double) intersection / union;
    }

    /**
     * Returns elements in setA that are NOT in setB (missing from B perspective).
     */
    public static Set<String> difference(Set<String> setA, Set<String> setB) {
        return setA.stream()
            .filter(elem -> !setB.contains(elem))
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Returns elements common to both sets.
     */
    public static Set<String> intersection(Set<String> setA, Set<String> setB) {
        return setA.stream()
            .filter(setB::contains)
            .collect(java.util.stream.Collectors.toSet());
    }

    private JaccardSimilarityUtil() {}
}
