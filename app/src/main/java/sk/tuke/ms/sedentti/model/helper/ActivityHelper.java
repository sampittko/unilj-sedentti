package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class ActivityHelper {
    private static final String TAG = "ActivityHelper";

    private Dao<Activity, Long> activityDao;
    private QueryBuilder<Activity, Long> activityDaoQueryBuilder;

    public ActivityHelper(Context context) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            activityDao = databaseHelper.activityDao();
            activityDaoQueryBuilder = activityDao.queryBuilder();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param type Type of the activity to create
     * @param session Session which the new activity belongs to
     * @throws SQLException In case that communication with DB was not successful
     */
    public void create(int type, @NotNull Session session) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing create");
        Crashlytics.log(Log.DEBUG, TAG, "@type: " + type);
        Crashlytics.log(Log.DEBUG, TAG, "@session ID: " + session.getId());

        Activity newActivity = new Activity(
                type,
                new Date().getTime(),
                session
        );

        activityDao.create(newActivity);
    }

    /**
     * @return Activity with the highest timestamp
     * @throws SQLException In case that communication with DB was not successful
     */
    public Activity getLast() throws SQLException, NullPointerException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getLast");

        activityDaoQueryBuilder.reset();

        Activity activity = activityDaoQueryBuilder
                .orderBy(Activity.COLUMN_TIMESTAMP, false)
                .queryForFirst();

        if (activity == null) {
            throw new NullPointerException();
        } else {
            return activity;
        }
    }

    /**
     * @param session
     * @return
     * @throws SQLException
     */
    public ArrayList<Activity> getCorresponding(@NotNull Session session) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getCorresponding");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID: " + session.getId());

        activityDaoQueryBuilder.reset();

        return new ArrayList<>(
                activityDaoQueryBuilder
                        .where()
                        .eq(Activity.COLUMN_SESSION_ID, session.getId())
                        .query()
        );
    }

    /**
     * @param session
     * @return
     * @throws SQLException
     */
    public void discardCorresponding(@NotNull Session session) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing discardCorresponding");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID: " + session.getId());

        activityDaoQueryBuilder.reset();

        ArrayList<Activity> activities = new ArrayList<>(
                activityDaoQueryBuilder
                        .where()
                        .eq(Activity.COLUMN_SESSION_ID, session.getId())
                        .query()
        );

        activityDao.delete(activities);
    }

//    @Contract(pure = true)
//    public static boolean isPassive(int activityType) {
//        return activityType == DetectedActivity.STILL;
//    }

    /**
     * @param activity
     * @throws SQLException
     */
    public void update(Activity activity) throws SQLException {
        activityDao.update(activity);
    }
}
