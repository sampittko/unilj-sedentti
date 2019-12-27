package sk.tuke.ms.sedentti.recognition.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import sk.tuke.ms.sedentti.config.PredefinedValues;

public class ActivityRecognitionHandler {

    private final String TAG = ActivityRecognitionHandler.class.getSimpleName();
    private Context context;
    private PendingIntent pendingIntent;

    public ActivityRecognitionHandler(Context context) {
        this.context = context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void startTracking() {
        List<ActivityTransition> transitions = getActivityTransitions();

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        Intent intent = new Intent(PredefinedValues.ACTIVITY_RECOGNITION_COMMAND);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Task<Void> task = ActivityRecognition.getClient(this.context)
                .requestActivityTransitionUpdates(request, pendingIntent);
//        Task<Void> task = ActivityRecognition.getClient(this.context)
//                .requestActivityUpdates(ACTIVITY_RECOGNITION_API_STEP_DELAY_MILLIS, pendingIntent);

        task.addOnSuccessListener(
                result -> Crashlytics.log(Log.DEBUG, TAG, "Activity tracking started")
        );

        task.addOnFailureListener(
                e -> Crashlytics.log(Log.DEBUG, TAG, "Activity tracking error starting" + e)
        );

    }

    public void stopTracking() {
        Task<Void> task = ActivityRecognition.getClient(this.context)
                .removeActivityTransitionUpdates(pendingIntent);
//        Task<Void> task = ActivityRecognition.getClient(this.context)
//                .removeActivityUpdates(pendingIntent);

        task.addOnSuccessListener(
                result -> {
                    pendingIntent.cancel();
                    Crashlytics.log(Log.DEBUG, TAG, "Activity tracking stopped");
                }
        );

        task.addOnFailureListener(
                e -> Crashlytics.log(Log.DEBUG, TAG, "Activity tracking error stop" + e)
        );
    }


    @NotNull
    private List<ActivityTransition> getActivityTransitions() {
        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_FOOT)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_FOOT)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        return transitions;
    }
}
