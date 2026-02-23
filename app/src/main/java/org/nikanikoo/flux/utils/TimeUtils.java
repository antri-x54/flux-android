package org.nikanikoo.flux.utils;

public class TimeUtils {
    public static String formatTimeAgo(long timestamp) {
        if (timestamp <= 0) {
            return "недавно";
        }

        long currentTime = System.currentTimeMillis() / 1000;
        long diff = currentTime - timestamp;

        if (diff < 0) {
            return "недавно";
        }

        if (diff < 60) {
            return "только что";
        }

        if (diff < 3600) {
            long minutes = diff / 60;
            return minutes + " мин назад";
        }

        if (diff < 86400) {
            long hours = diff / 3600;
            return hours + " ч назад";
        }

        long days = diff / 86400;
        return days + " дн назад";
    }
}
