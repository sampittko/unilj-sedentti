package sk.tuke.ms.sedentti.recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Date;

import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;
import sk.tuke.ms.sedentti.model.helper.ActivityHelper;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {
    private Dao<Profile, Long> profileDao;
    private Dao<Activity, Long> activityDao;
    private Dao<Session, Long> sessionDao;

    private Profile activeProfile;
    private Session activeSession;
    private Activity currentActivity;

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

                    if (isNewSessionRequired(activityType)) {
                        endActiveSession();
                        setNewActiveSession(activityType, timestamp);
                    }

                    if (isNewActivityRequired(transitionType)) {
                        endCurrentActivity();
                        setNewActivity(activityType, timestamp);
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
            activeProfile = new ProfileHelper(context).getActiveProfile();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isNewSessionRequired(int newActivityType) {
        // the first broadcast received => values are not set yet
        if (activeSession == null && currentActivity == null) {
            return true;
        }

        // the user gets active from still or gets still from active
        return newActivityType == DetectedActivity.STILL || currentActivity.getActivityType() == DetectedActivity.STILL;
    }
    private void endActiveSession() {
        if (activeSession != null) {
            try {
                sessionDao.update(
                        SessionHelper.updateAsEndedSession(activeSession)
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void setNewActiveSession(int activityType, long timestamp) {
        activeSession = new Session(
                SessionHelper.isSedentary(activityType),
                timestamp,
                activeProfile
        );

        try {
            sessionDao.create(activeSession);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isNewActivityRequired(int transitionType) {
        return (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) || (currentActivity == null);
    }

    private void endCurrentActivity() {
        if (currentActivity != null) {
            try {
                activityDao.update(
                        ActivityHelper.updateAsEndedActivity(currentActivity)
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void setNewActivity(int activityType, long timestamp) {
        currentActivity = new Activity(activityType, timestamp, activeSession);

        try {
            activityDao.create(currentActivity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
