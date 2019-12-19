package sk.tuke.ms.sedentti.recognition.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.jetbrains.annotations.Contract;

import java.sql.SQLException;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.activity.MainActivity;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.helper.shared_preferences.ActivityRecognitionSPHelper;
import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.ActivityHelper;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.notification.StopSedentaryNotification;
import sk.tuke.ms.sedentti.recognition.motion.SignificantMotionDetector;
import sk.tuke.ms.sedentti.recognition.motion.SignificantMotionListener;

public class ActivityRecognitionService extends Service implements SignificantMotionListener {

    private static final int SERVICE_NOTIFICATION_ID = 1;
    private static final int MOTION_NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "sk.tuke.ms.sedentti";
    private static final String TAG = "ARService";

    private final int TIME_STEP = 300;
    private SessionHelper sessionHelper;

    private NotificationManager notificationManager;
    private ActivityRecognitionHandler activityRecognitionHandler;
    private ActivityRecognitionBroadcastReceiver receiver;
    private ActivityRecognitionSPHelper activityRecognitionPreferences;

    private SignificantMotionDetector significantMotionDetector;
    private Session currentSession;
    private Handler activityHandler;
    private ActivityHelper activityHelper;
    private Runnable activityChanged = new Runnable() {
        @Override
        public void run() {
            // TODO: 12/18/19 get new session from db
            // TODO: 12/18/19 start new counting
        }
    };
    private long time;
    private Handler timeHandler;
    private Runnable countTime = new Runnable() {
        @Override
        public void run() {
            time += TIME_STEP;
            processTimeDependency();
            timeHandler.postDelayed(countTime, TIME_STEP);
        }
    };

    private void processTimeDependency() {
        // TODO: 12/18/19 handle notification, sigmov etc
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int commandResult = processCommand(intent);
        startForeground(SERVICE_NOTIFICATION_ID, createNotification(commandResult));

        return super.onStartCommand(intent, flags, startId);
    }

    private int processCommand(Intent intent) {
//        if state is unknown, service is only started without sensing
        if (intent == null || intent.getAction() == null) {
            return PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_UNKNOWN;
        }

        switch (intent.getAction()) {
            case PredefinedValues.COMMAND_INIT:
                // this is called  each time app is opened
                int state = this.activityRecognitionPreferences.getActivityRecognitionState();

                if (state == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING) {
                    // if the service was running and it was saved in shared preferences and make sure it runs
                    serviceToggle(PredefinedValues.COMMAND_START);
                } else if (state == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED) {
                    // if the service was running and it was saved in shared preferences and make sure it has stopped
                    serviceToggle(PredefinedValues.COMMAND_STOP);
                } else {
                    // otherwise, first time run, some problem etc
                    // TODO: 12/11/19 check this line
                    // register receiver just avoid the case when there is no registered register
//                    registerReceiver(receiver, new IntentFilter(PredefinedValues.ACTIVITY_RECOGNITION_COMMAND));
                    serviceToggle(PredefinedValues.COMMAND_STOP);
                    this.activityRecognitionPreferences.saveStateToSharedPreferences(PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED);
                }

                return state;
            case PredefinedValues.COMMAND_START:
                serviceToggle(PredefinedValues.COMMAND_START);
                this.activityRecognitionPreferences.saveStateToSharedPreferences(PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING);

                return PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING;
            case PredefinedValues.COMMAND_STOP:
                serviceToggle(PredefinedValues.COMMAND_STOP);
                this.activityRecognitionPreferences.saveStateToSharedPreferences(PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED);

                return PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED;
        }
        return PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_UNKNOWN;
    }

    private void serviceToggle(String command) {
        if (command.equals(PredefinedValues.COMMAND_START)) {
            registerReceiver(receiver, new IntentFilter(PredefinedValues.ACTIVITY_RECOGNITION_COMMAND));
            this.activityRecognitionHandler.startTracking();
            // TODO: 12/19/19 nie vzyd treba zapnnut 
            this.significantMotionDetector.start();
            Log.i(TAG, "Sensing service started");
        } else if (command.equals(PredefinedValues.COMMAND_STOP)) {
            this.activityRecognitionHandler.stopTracking();
            this.significantMotionDetector.stop();
            try {
                unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Sensing service stopped");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.receiver = new ActivityRecognitionBroadcastReceiver();
        this.activityRecognitionHandler = new ActivityRecognitionHandler(context);
        this.activityRecognitionPreferences = new ActivityRecognitionSPHelper(context);
        this.significantMotionDetector = new SignificantMotionDetector(context, this);
        this.activityHandler = new Handler();

        initDatabaseForService(context);
    }

    private void initDatabaseForService(Context context) {
        Profile profile = null;

        try {
            profile = new ProfileHelper(context).getActive();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.sessionHelper = new SessionHelper(context, profile);
        this.activityHelper = new ActivityHelper(context);
    }

    @Override
    public void onDestroy() {
        activityRecognitionHandler.stopTracking();
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Contract(pure = true)
    private SessionHelper getSessionHelper() {
        return sessionHelper;
    }

    @Contract(pure = true)
    private ActivityHelper getActivityHelper() {
        return activityHelper;
    }

    private Notification createNotification(int commandResult) {
        // TODO: 12/18/19 move this notification to separate class

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            od Orea vyssie
            CharSequence channelName = getString(R.string.app_name);

            if (notificationManager != null) {
                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);

                if (notificationChannel == null) {
                    notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }
//            Nougat a nizsie
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_person_outline_black_24dp)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setShowWhen(false);


        Intent openingIntent = new Intent(this, MainActivity.class);
        openingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openingPendingIntent = PendingIntent.getActivity(this, 0, openingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(openingPendingIntent);

        builder.setContentTitle("Sedentti");

        String state = null;
        if (commandResult == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING) {
            state = "Sedentti is tracking your sitting";
        } else if (commandResult == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED) {
            state = "Sedentti is not active";
        }
        if (state != null) {
            builder.setContentText(state);
        }

        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleSignificantMotion(int newActivityType) {
        Crashlytics.log(Log.DEBUG, TAG, "Handling significant motion");
        if (ActivityHelper.isPassive(newActivityType)) {
            this.significantMotionDetector.start();
        } else {
            this.significantMotionDetector.stop();
        }
    }

    @Override
    public void onSignificantMotionDetected() {
        Crashlytics.log(Log.DEBUG, TAG, "Significant motion detected");
        try {
            sessionHelper.replacePendingWithNew(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        new StopSedentaryNotification().createNotification(getApplicationContext(), MOTION_NOTIFICATION_ID);
    }

    public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {
        private final String TAG = "ARBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult intentResult = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : Objects.requireNonNull(intentResult).getTransitionEvents()) {
                    int newActivityType = event.getActivityType();
                    int newActivityTransitionType = event.getTransitionType();

                    Crashlytics.log(Log.DEBUG, TAG, "New activity with type " + newActivityType + " and transition " +
                            newActivityTransitionType + " received");

                    SessionHelper sessionHelper = getSessionHelper();
                    ActivityHelper activityHelper = getActivityHelper();

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
                                handleSignificantMotion(newActivityType);

                                if (pendingSession != null) {
                                    sessionHelper.end(pendingSession);
                                    Crashlytics.log(Log.DEBUG, TAG, "Pending session closed");
                                }

                                pendingSession = sessionHelper.create(newActivityType);
                                Crashlytics.log(Log.DEBUG, TAG, "New session created");
                            } else {
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

        @Contract("_, null -> true")
        private boolean hasActivityChanged(int newActivityType, Activity lastActivity) {
            if (lastActivity == null) {
                Crashlytics.log(Log.DEBUG, TAG, "Activity has changed");
                return true;
            }
            Crashlytics.log(Log.DEBUG, TAG, "Activity has not changed");
            return (newActivityType == DetectedActivity.STILL && lastActivity.getType() != DetectedActivity.STILL)
                    || (lastActivity.getType() == DetectedActivity.STILL && newActivityType != DetectedActivity.STILL);
        }
    }
}
