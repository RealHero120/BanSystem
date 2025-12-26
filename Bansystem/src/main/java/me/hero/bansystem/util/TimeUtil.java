package me.hero.bansystem.util;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");

    public static long parseDuration(String input) {
        if (input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent")) return -1;
        Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase());
        long totalMillis = 0;
        boolean found = false;
        while (matcher.find()) {
            found = true;
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            totalMillis += switch (unit) {
                case "s" -> amount * 1000;
                case "m" -> amount * 60000;
                case "h" -> amount * 3600000;
                case "d" -> amount * 86400000;
                default -> 0;
            };
        }
        return found ? totalMillis : -2;
    }

    public static String formatDuration(long millis) {
        if (millis == -1) return "Permanent";
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}