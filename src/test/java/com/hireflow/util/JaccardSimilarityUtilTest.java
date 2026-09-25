package com.hireflow.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JaccardSimilarityUtilTest {

    @Test
    void computeReturns1WhenSetsAreEqual() {
        Set<String> a = Set.of("java", "spring", "mysql");
        assertThat(JaccardSimilarityUtil.compute(a, a)).isEqualTo(1.0);
    }

    @Test
    void computeReturns0WhenNoOverlap() {
        Set<String> a = Set.of("java", "spring");
        Set<String> b = Set.of("python", "django");
        assertThat(JaccardSimilarityUtil.compute(a, b)).isEqualTo(0.0);
    }

    @Test
    void computeReturnsCorrectScore() {
        Set<String> a = Set.of("java", "spring", "mysql", "redis");
        Set<String> b = Set.of("java", "spring", "python");
        // intersection = {java, spring} = 2
        // union = {java, spring, mysql, redis, python} = 5
        // score = 2/5 = 0.4
        double score = JaccardSimilarityUtil.compute(a, b);
        assertThat(score).isCloseTo(0.4, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void computeReturnsBothEmptyAs1() {
        assertThat(JaccardSimilarityUtil.compute(Set.of(), Set.of())).isEqualTo(1.0);
    }

    @Test
    void computeReturns0WhenOneIsEmpty() {
        assertThat(JaccardSimilarityUtil.compute(Set.of("java"), Set.of())).isEqualTo(0.0);
    }

    @Test
    void differenceReturnsCorrectMissingElements() {
        Set<String> required = Set.of("java", "spring", "docker");
        Set<String> seeker = Set.of("java", "spring");
        Set<String> missing = JaccardSimilarityUtil.difference(required, seeker);
        assertThat(missing).containsExactlyInAnyOrder("docker");
    }

    @Test
    void intersectionReturnsCommonElements() {
        Set<String> a = Set.of("java", "spring", "mysql");
        Set<String> b = Set.of("java", "python", "mysql");
        Set<String> common = JaccardSimilarityUtil.intersection(a, b);
        assertThat(common).containsExactlyInAnyOrder("java", "mysql");
    }
}
