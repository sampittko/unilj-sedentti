package sk.tuke.ms.sedentti.recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.ActivityHelper;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {
    private SessionHelper sessionHelper;
    private ActivityHelper activityHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult intentResult = ActivityTransitionResult.extractResult(intent);

                try {
                    performInitialSetup(context);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                for (ActivityTransitionEvent event : intentResult.getTransitionEvents()) {
                    int newActivityType = event.getActivityType();
                    int newActivityTransitionType = event.getTransitionType();

                    try {
                        Activity lastActivity = activityHelper.getLastActivity();
                        Session pendingSession = sessionHelper.getPendingSession();

                        // new activity has started
                        if (newActivityTransitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                            // activity has changed (active -> still; still -> active)
                            if (hasActivityChanged(newActivityType, lastActivity)) {
                                // close pending session
                                if (pendingSession != null) {
                                    sessionHelper.updateAsEndedSession(pendingSession);
                                }

                                // create new session
                                pendingSession = getNewSession(newActivityType);

                                // create new activity in connection with the new session
                                activityHelper.createActivity(newActivityType, pendingSession);
                            }
                            // activity has not changed (still -> still; active -> active)
                            else {
                                // create new session
                                if (pendingSession == null) {
                                    sessionHelper.createSession(newActivityType);
                                    pendingSession = sessionHelper.getPendingSession();
                                }

                                activityHelper.createActivity(newActivityType, pendingSession);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void performInitialSetup(Context context) throws SQLException {
        if (activityHelper == null || sessionHelper == null) {
            activityHelper = new ActivityHelper(context);
            sessionHelper = new SessionHelper(
                    context,
                    new ProfileHelper(context).getActiveProfile()
            );
        }
    }

    private boolean hasActivityChanged(int newActivityType, Activity lastActivity) {
        if (lastActivity == null) {
            return true;
        }

        return (newActivityType == DetectedActivity.STILL && lastActivity.getType() != DetectedActivity.STILL)
                || (lastActivity.getType() == DetectedActivity.STILL && newActivityType != DetectedActivity.STILL);
    }

    private Session getNewSession(int newActivityType) throws SQLException {
        sessionHelper.createSession(newActivityType);
        return sessionHelper.getPendingSession();
    }
}
