package sk.tuke.ms.sedentti.recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Date;

import sk.tuke.ms.sedentti.helper.CommonStrings;
import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {
    private Dao<Profile, Long> profileDao;
    private Dao<Activity, Long> activityDao;
    private Dao<Session, Long> sessionDao;

    private Profile activeProfile;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult intentResult = ActivityTransitionResult.extractResult(intent);

                databaseSetup(context);

                for (ActivityTransitionEvent event : intentResult.getTransitionEvents()) {
                    int activityType = event.getActivityType();
                    int transitionType = event.getTransitionType();
                    Date timestamp = new Date();

                    // TODO handle session
                    Session dummySession = new Session(false, timestamp.getTime(), activeProfile);
                    Activity activity = new Activity(activityType, transitionType, timestamp.getTime(), dummySession);

                    try {
                        // TMP dummy session
                        sessionDao.create(dummySession);
                        activityDao.create(activity);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void databaseSetup(Context context) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

        try {
            activityDao = databaseHelper.activityDao();
            sessionDao = databaseHelper.sessionDao();
            profileDao = databaseHelper.profileDao();
            activeProfile = getActiveProfile(context);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Profile getActiveProfile(Context context) throws SQLException {
        long activeProfileId = getActiveProfileId(context);
        return profileDao.queryForId(activeProfileId);
    }

    private long getActiveProfileId(Context context) {
        SharedPreferences profileShPr = context.getSharedPreferences(CommonStrings.PROFILE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return profileShPr.getLong(CommonStrings.PROFILE_SHARED_PREFERENCES_ACTIVE_ID, Integer.valueOf("0"));
    }
}
