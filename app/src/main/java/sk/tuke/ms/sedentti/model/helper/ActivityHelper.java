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

    /**
     * @param activity Activity to update as the ended one
     * @throws SQLException In case that communication with DB was not successful
     */
    public void updateAsEndedActivity(@NotNull Activity activity) throws SQLException {
        long endTimestamp = new Date().getTime();

        activity.setDuration(
                getActivityDuration(activity.getTimestamp(), endTimestamp)
        );

        updateActivity(activity);
    }

    @Contract(pure = true)
    private static long getActivityDuration(long timestamp, long endTimestamp) {
        return endTimestamp - timestamp;
    }

    /**
     * @param activity Activity to update
     * @throws SQLException In case that communication with DB was not successful
     */
    public void updateActivity(Activity activity) throws SQLException {
        activityDao.update(activity);
    }

    /**
     * @param activity Activity to create
     * @throws SQLException In case that communication with DB was not successful
     */
    public void createActivity(Activity activity) throws SQLException {
        activityDao.create(activity);
    }
}
