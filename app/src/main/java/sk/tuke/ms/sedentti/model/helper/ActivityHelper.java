package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Date;

import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class ActivityHelper {
    private Dao<Activity, Long> activityDao;

    public ActivityHelper(Context context) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            activityDao = databaseHelper.activityDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    public void updateActivity(Activity activity) throws SQLException {
        activityDao.update(activity);
    }

    public void createActivity(Activity activity) throws SQLException {
        activityDao.create(activity);
    }
}
