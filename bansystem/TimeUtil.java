package me.hero.bansystem;

import java.util.Locale;

public final class TimeUtil {

    public static long parseDuration(String input) {
        if (input == null) return -1;
        String s = input.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return -1;

        long total = 0;
        StringBuilder num = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isWhitespace(c))
                continue;

            if (Character.isDigit(c)) {
                num.append(c);
                continue;
            }

            if (num.length() == 0)
                return -1;

            long val = Long.parseLong(num.toString());
            num.setLength(0);

            switch (c) {
                case 's' -> total += val * 1000L;
                case 'm' -> total += val * 60000L;
                case 'h' -> total += val * 3600000L;
                case 'd' -> total += val * 86400000L;
                default -> { return -1; }
            }
        }

        if (num.length() > 0)
            total += Long.parseLong(num.toString()) * 60000L;

        return total > 0 ? total : -1;
    }

    public static String formatDuration(long ms) {
        if (ms <= 0) return "0s";

        long s = ms / 1000;

        long d = s / 86400; s %= 86400;
        long h = s / 3600;  s %= 3600;
        long m = s / 60;    s %= 60;

        String out = "";
        if (d > 0) out += d + "d ";
        if (h > 0) out += h + "h ";
        if (m > 0) out += m + "m ";
        if (s > 0) out += s + "s";

        return out.trim();
    }
}
