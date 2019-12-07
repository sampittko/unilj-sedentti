package sk.tuke.ms.sedentti.model.helper;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

public class DateHelper {
    private final static long daySeconds = 86400;
    private final static long weekSeconds = daySeconds * 7;
    private final static long monthSeconds = daySeconds * 30;

    /**
     * @return
     */
    @NotNull
    public static Date getLastDay() {
        long todayTimestamp = new Date().getTime();
        return new Date(todayTimestamp - daySeconds);
    }

    /**
     * @param from
     * @return
     */
    @NotNull
    public static Date getLastDay(@NotNull Date from) {
        long fromTimestamp = from.getTime();
        return new Date(fromTimestamp - daySeconds);
    }

    /**
     * @return
     */
    @NotNull
    public static Date getLastWeek() {
        long todayTimestamp = new Date().getTime();
        return new Date(todayTimestamp - weekSeconds);
    }

    /**
     * @param from
     * @return
     */
    @NotNull
    public static Date getLastWeek(@NotNull Date from) {
        long fromTimestamp = from.getTime();
        return new Date(fromTimestamp - weekSeconds);
    }

    /**
     * @return
     */
    @NotNull
    public static Date getLastMonth() {
        long todayTimestamp = new Date().getTime();
        return new Date(todayTimestamp - monthSeconds);
    }

    /**
     * @param from
     * @return
     */
    @NotNull
    public static Date getLastMonth(@NotNull Date from) {
        long fromTimestamp = from.getTime();
        return new Date(fromTimestamp - monthSeconds);
    }

    /**
     * @param date
     * @return
     */
    public static Date getNormalized(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        return calendar.getTime();
    }

    /**
     * @param calendar
     * @return
     */
    public static int getDay(@NotNull Calendar calendar) {
        return calendar.get(Calendar.DATE);
    }

    /**
     * @param calendar
     * @return
     */
    public static int getMonth(@NotNull Calendar calendar) {
        return calendar.get(Calendar.MONTH);
    }

    /**
     * @param calendar
     * @return
     */
    public static int getYear(@NotNull Calendar calendar) {
        return calendar.get(Calendar.YEAR);
    }
}
