package com.library.utils;

import java.util.List;
import java.util.function.Function;

/**
 * Generates the next sequential ID for books ("B001", "B002", ...),
 * users ("U001", ...) and transactions ("T001", ...) by inspecting the
 * highest numeric suffix currently in use and incrementing it. This keeps
 * IDs stable and human-readable instead of using random UUIDs.
 */
public final class IDGenerator {

    private IDGenerator() { /* utility class */ }

    public static <T> String generate(String prefix, List<T> existing, Function<T, String> idExtractor) {
        int max = 0;
        for (T item : existing) {
            String id = idExtractor.apply(item);
            if (id != null && id.startsWith(prefix)) {
                try {
                    int n = Integer.parseInt(id.substring(prefix.length()));
                    max = Math.max(max, n);
                } catch (NumberFormatException ignored) {
                    // non-numeric suffix — skip, keep scanning
                }
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }
}
