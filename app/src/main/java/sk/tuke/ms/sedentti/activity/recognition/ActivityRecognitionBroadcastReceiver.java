package sk.tuke.ms.sedentti.activity.recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.helper.CommonStrings;
import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && CommonStrings.ACTIVITY_RECOGNITION_COMMAND.equals(intent.getAction())) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult intentResult = ActivityTransitionResult
                        .extractResult(intent);
                // handle activity transition result ...
                DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

                Dao<Activity, Long> activityDao = null;

                try {
                    activityDao = databaseHelper.activityDao();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                for (ActivityTransitionEvent event : intentResult.getTransitionEvents()) {
                    Activity activity = new Activity();

                    activity.setActivityType(event.getActivityType());
                    activity.setTransitionType(event.getTransitionType());
                    activity.setElapsedRealTimeNanos(event.getElapsedRealTimeNanos());

                    try {
                        activityDao.create(activity);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}