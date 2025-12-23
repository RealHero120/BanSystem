package me.hero.bansystem.util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class TimeUtil {

    /**
     * Parses a string like "1d2h" into milliseconds.
     * If no unit is provided (e.g., "10"), it defaults to minutes.
     */
    public static long parseDuration(String input) {
        if (input == null) return -1;
        String s = input.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return -1;

        long total = 0;
        StringBuilder num = new StringBuilder();
        boolean unitFound = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                num.append(c);
            } else {
                if (num.length() == 0) return -1;

                long val = Long.parseLong(num.toString());
                num.setLength(0);
                unitFound = true;

                switch (c) {
                    case 's' -> total += val * 1000L;
                    case 'm' -> total += val * 60000L;
                    case 'h' -> total += val * 3600000L;
                    case 'd' -> total += val * 86400000L;
                    case 'w' -> total += val * 604800000L;
                    default -> { return -1; }
                }
            }
        }

        // If the string ends in a number (like "10"), treat that part as minutes
        if (num.length() > 0) {
            total += Long.parseLong(num.toString()) * 60000L;
        }

        return total > 0 ? total : -1;
    }

    public static String formatDuration(long ms) {
        if (ms <= 0) return "0s";

        long days = TimeUnit.MILLISECONDS.toDays(ms);
        ms -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(ms);
        ms -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
        ms -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }
}