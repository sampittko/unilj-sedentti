package sk.tuke.ms.sedentti.model.helper;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

class DateHelper {
    private final static long daySeconds = 86400;
    private final static long weekSeconds = daySeconds * 7;
    private final static long monthSeconds = daySeconds * 30;

    @NotNull
    static Date getLastDay() {
        long todayTimestamp = new Date().getTime();
        return new Date(todayTimestamp - daySeconds);
    }

    @NotNull
    static Date getLastDay(@NotNull Date from) {
        long fromTimestamp = from.getTime();
        return new Date(fromTimestamp - daySeconds);
    }

    @NotNull
    static Date getLastWeek() {
        long todayTimestamp = new Date().getTime();
        return new Date(todayTimestamp - weekSeconds);
    }

    @NotNull
    static Date getLastWeek(@NotNull Date from) {
        long fromTimestamp = from.getTime();
        return new Date(fromTimestamp - weekSeconds);
    }

    @NotNull
    static Date getLastMonth() {
        long todayTimestamp = new Date().getTime();
        return new Date(todayTimestamp - monthSeconds);
    }

    @NotNull
    static Date getLastMonth(@NotNull Date from) {
        long fromTimestamp = from.getTime();
        return new Date(fromTimestamp - monthSeconds);
    }
}
