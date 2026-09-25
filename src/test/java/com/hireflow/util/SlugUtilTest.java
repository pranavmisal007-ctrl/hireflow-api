package com.hireflow.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugUtilTest {

    @Test
    void generateSlugFromSimpleText() {
        assertThat(SlugUtil.generateSlug("Java Developer")).isEqualTo("java-developer");
    }

    @Test
    void generateSlugRemovesSpecialChars() {
        assertThat(SlugUtil.generateSlug("Senior Java/Kotlin Dev!")).isEqualTo("senior-javakotlin-dev");
    }

    @Test
    void generateSlugHandlesMultipleSpaces() {
        assertThat(SlugUtil.generateSlug("  Hello   World  ")).isEqualTo("hello-world");
    }

    @Test
    void generateSlugReturnsEmptyForBlank() {
        assertThat(SlugUtil.generateSlug("")).isEqualTo("");
        assertThat(SlugUtil.generateSlug(null)).isEqualTo("");
    }

    @Test
    void generateUniqueSlugReturnBaseWhenNotExists() {
        String slug = SlugUtil.generateUniqueSlug("Java Dev", s -> false);
        assertThat(slug).isEqualTo("java-dev");
    }

    @Test
    void generateUniqueSlugAppendsCounterWhenExists() {
        // Simulate first slug taken
        String slug = SlugUtil.generateUniqueSlug("Java Dev", s -> s.equals("java-dev"));
        assertThat(slug).isEqualTo("java-dev-1");
    }

    @Test
    void generateUniqueSlugIncrementsUntilFree() {
        String slug = SlugUtil.generateUniqueSlug("Dev",
            s -> s.equals("dev") || s.equals("dev-1") || s.equals("dev-2"));
        assertThat(slug).isEqualTo("dev-3");
    }
}
