package sk.tuke.ms.sedentti.model.helper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

import sk.tuke.ms.sedentti.model.Activity;

public class ActivityHelper {
    @Contract("_ -> param1")
    public static Activity updateAsEndedActivity(@NotNull Activity activity) {
        long endTimestamp = new Date().getTime();

        activity.setDuration(
                getActivityDuration(activity.getTimestamp(), endTimestamp)
        );

        return activity;
    }

    @Contract(pure = true)
    private static long getActivityDuration(long timestamp, long endTimestamp) {
        return endTimestamp - timestamp;
    }
}
