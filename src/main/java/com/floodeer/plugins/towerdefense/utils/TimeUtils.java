package com.floodeer.plugins.towerdefense.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_DAY = "yyyy-MM-dd";

    public static String now() {
        Calendar localCalendar = Calendar.getInstance();
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return localSimpleDateFormat.format(localCalendar.getTime());
    }

    public static String time() {
        Calendar localCalendar = Calendar.getInstance();
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return localSimpleDateFormat.format(localCalendar.getTime());
    }

    public static String when(long paramLong) {
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return localSimpleDateFormat.format(paramLong);
    }

    public static String date() {
        Calendar localCalendar = Calendar.getInstance();
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return localSimpleDateFormat.format(localCalendar.getTime());
    }

    public static String formatScoreboard(int s) {
        Date date = new Date(s * 1000L);
        return new SimpleDateFormat("mm:ss").format(date);
    }


    public enum TimeUnit {
        FIT, DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS
    }

    public static boolean elapsed(long paramLong1, long paramLong2) {
        return System.currentTimeMillis() - paramLong1 > paramLong2;
    }
}