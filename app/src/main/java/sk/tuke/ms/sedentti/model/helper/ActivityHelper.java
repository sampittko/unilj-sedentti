package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.Date;

import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class ActivityHelper {
    private Dao<Activity, Long> activityDao;
    private QueryBuilder<Activity, Long> activityDaoQueryBuilder;

    public ActivityHelper(Context context) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            activityDao = databaseHelper.activityDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param activity Activity to update
     * @throws SQLException In case that communication with DB was not successful
     */
    public void updateActivity(Activity activity) throws SQLException {
        activityDao.update(activity);
    }

    /**
     * @param type Type of the activity to create
     * @param session Session which the new activity belongs to
     * @throws SQLException In case that communication with DB was not successful
     */
    public void createActivity(int type, Session session) throws SQLException {
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
    public Activity getLastActivity() throws SQLException {
        return activityDaoQueryBuilder
                .orderBy(Activity.COLUMN_TIMESTAMP, false)
                .queryForFirst();
    }
}
