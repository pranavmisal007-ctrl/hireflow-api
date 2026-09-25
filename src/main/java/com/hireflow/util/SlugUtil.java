package com.hireflow.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-|-$)");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-{2,}");

    public static String generateSlug(String input) {
        if (input == null || input.isBlank()) return "";

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = normalized.toLowerCase();
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        slug = MULTIPLE_DASHES.matcher(slug).replaceAll("-");
        slug = EDGE_DASHES.matcher(slug).replaceAll("");

        return slug;
    }

    public static String generateUniqueSlug(String base, java.util.function.Function<String, Boolean> existsChecker) {
        String slug = generateSlug(base);
        if (!existsChecker.apply(slug)) return slug;

        int counter = 1;
        while (existsChecker.apply(slug + "-" + counter)) {
            counter++;
        }
        return slug + "-" + counter;
    }

    private SlugUtil() {}
}
