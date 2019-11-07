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
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {
    private Dao<Profile, Long> profileDao;
    private Dao<Activity, Long> activityDao;
    private Dao<Session, Long> sessionDao;

    private Profile activeProfile;
    private Session activeSession;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult intentResult = ActivityTransitionResult.extractResult(intent);

                databaseSetup(context);

                for (ActivityTransitionEvent event : intentResult.getTransitionEvents()) {
                    int activityType = event.getActivityType();
                    int transitionType = event.getTransitionType();
                    long timestamp = new Date().getTime();

                    if (isNewSessionRequired(activityType, transitionType)) {
                        endActiveSession();
                        activeSession = getNewActiveSession(activityType, timestamp);
                    }

                    persistNewActivity(activityType, transitionType, timestamp, activeSession);
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

    // TODO new session requirement evaluation
    private boolean isNewSessionRequired(int activityType, int transitionType) {
        if (activeSession == null) {
            return true;
        }

//        if (previousActivity.activityType == STILL);
//        if (previousActivity.transitionType == ..);
//
//        ActivityTransition.ACTIVITY_TRANSITION_ENTER;
//        ActivityTransition.ACTIVITY_TRANSITION_EXIT;
//        DetectedActivity.STILL;

        return false;
    }

    private void endActiveSession() {
        try {
            sessionDao.update(
                    SessionHelper.updateAsEndedSession(activeSession)
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Session getNewActiveSession(int activityType, long timestamp) {
        Session newSession = new Session(
                SessionHelper.isSedentary(activityType),
                timestamp,
                activeProfile
        );

        try {
            sessionDao.create(newSession);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newSession;
    }

    private void persistNewActivity(int activityType, int transitionType, long timestamp, Session activeSession) {
        Activity activity = new Activity(activityType, transitionType, timestamp, activeSession);

        try {
            activityDao.create(activity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
