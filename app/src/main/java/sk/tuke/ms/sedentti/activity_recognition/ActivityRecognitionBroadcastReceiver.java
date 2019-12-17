package sk.tuke.ms.sedentti.activity_recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.jetbrains.annotations.Contract;

import java.sql.SQLException;
import java.util.Objects;

import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.ActivityHelper;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {
    private SessionHelper sessionHelper;
    private ActivityHelper activityHelper;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult intentResult = ActivityTransitionResult.extractResult(intent);

            try {
                performInitialSetup(context);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (ActivityTransitionEvent event : Objects.requireNonNull(intentResult).getTransitionEvents()) {
                int newActivityType = event.getActivityType();
                int newActivityTransitionType = event.getTransitionType();

                Crashlytics.log(Log.DEBUG, TAG, "New activity with type " + newActivityType + " and transition " +
                        newActivityTransitionType + " received");

                try {
                    Activity lastActivity = activityHelper.getLast();
                    Session pendingSession = null;
                    try {
                        pendingSession = sessionHelper.getPending();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        Crashlytics.log(Log.DEBUG, TAG, "There is no pending session");
                    }

                    if (newActivityTransitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        Crashlytics.log(Log.DEBUG, TAG, "New activity has started");
                        if (hasActivityChanged(newActivityType, lastActivity)) {
                            Crashlytics.log(Log.DEBUG, TAG, "Activity has changed");
                            if (pendingSession != null) {
                                sessionHelper.end(pendingSession);
                                Crashlytics.log(Log.DEBUG, TAG, "Pending session closed");
                            }

                            pendingSession = sessionHelper.create(newActivityType);
                            Crashlytics.log(Log.DEBUG, TAG, "New session created");
                        }
                        else {
                            Crashlytics.log(Log.DEBUG, TAG, "Activity has not changed");
                            if (pendingSession == null) {
                                pendingSession = sessionHelper.create(newActivityType);
                                Crashlytics.log(Log.DEBUG, TAG, "New session created");
                            }
                        }

                        activityHelper.create(newActivityType, pendingSession);
                        Crashlytics.log(Log.DEBUG, TAG, "New activity created");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void performInitialSetup(Context context) throws SQLException {
        if (activityHelper == null) {
            activityHelper = new ActivityHelper(context);
        }

        if (sessionHelper == null) {
            sessionHelper = new SessionHelper(
                    context,
                    new ProfileHelper(context).getActive()
            );
        }
    }

    @Contract("_, null -> true")
    private boolean hasActivityChanged(int newActivityType, Activity lastActivity) {
        if (lastActivity == null) {
            return true;
        }

        return (newActivityType == DetectedActivity.STILL && lastActivity.getType() != DetectedActivity.STILL)
                || (lastActivity.getType() == DetectedActivity.STILL && newActivityType != DetectedActivity.STILL);
    }
}
