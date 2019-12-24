package sk.tuke.ms.sedentti.recognition.activity;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.helper.shared_preferences.ActivityRecognitionSPHelper;
import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.SessionType;
import sk.tuke.ms.sedentti.model.helper.ActivityHelper;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.notification.ServiceNotification;
import sk.tuke.ms.sedentti.notification.movement.MovementNotification;
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

    private AppSPHelper appPreferences;

    private SignificantMotionDetector significantMotionDetector;
    private Session currentSession;
    private ActivityHelper activityHelper;

    private boolean isActiveTimePassed;

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
        if (this.currentSession != null) {
            int activeLimit = this.appPreferences.getActiveLimit();

            if (!this.currentSession.isSedentary() && !this.currentSession.isInVehicle() && !this.isActiveTimePassed && this.time > activeLimit) {
                Crashlytics.log(Log.DEBUG, TAG, "Active time reached");
                this.isActiveTimePassed = true;
                try {
                    if (!this.sessionHelper.isPendingReal()) {
                        Crashlytics.log(Log.DEBUG, TAG, "Active session is artificial, replacing by the new sedentary");

                        Session newSession = sessionHelper.createAndReplacePending(true);
                        activityHelper.create(DetectedActivity.STILL, newSession);
                        handleSignificantMotion(DetectedActivity.STILL);
                        setCurrentSession(newSession);
                        notificationManager.cancel(MOTION_NOTIFICATION_ID);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // TODO: 12/23/19 add more notification
    }

    private void startTicking() {
        stopTicking();
        this.timeHandler.post(countTime);
    }

    private void updateCurrentSession() {
        try {
            this.currentSession = sessionHelper.getPending();
            this.time = currentSession.getDuration();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Crashlytics.log(Log.DEBUG, TAG, "There is no pending session at the moment");
        }
    }

    private void stopTicking() {
        this.timeHandler.removeCallbacks(countTime);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int commandResult = processCommand(intent);
        startForeground(SERVICE_NOTIFICATION_ID, new ServiceNotification().createNotification(getApplicationContext(), commandResult, SERVICE_NOTIFICATION_ID));

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
            updateCurrentSession();
            startTicking();
            if (this.currentSession != null) {
                if (this.currentSession.isSedentary()) {
                    this.significantMotionDetector.start();
                } else {
                    this.significantMotionDetector.stop();
                }
            }
            Crashlytics.log(Log.DEBUG, TAG, "Sensing service started");
        } else if (command.equals(PredefinedValues.COMMAND_STOP)) {
            this.activityRecognitionHandler.stopTracking();
            stopTicking();
            this.significantMotionDetector.stop();
            try {
                unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            Crashlytics.log(Log.DEBUG, TAG, "Sensing service stopped");
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
        this.timeHandler = new Handler();
        this.appPreferences = new AppSPHelper(context);

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

    private void setCurrentSession(Session session) {
        this.currentSession = session;
        this.time = this.sessionHelper.getDuration(session);

        this.isActiveTimePassed = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleSignificantMotion(int detectedActivity) {
        Crashlytics.log(Log.DEBUG, TAG, "Handling significant motion");
        if (sessionHelper.getSessionType(detectedActivity) == SessionType.SEDENTARY) {
            this.significantMotionDetector.start();
        } else {
            this.significantMotionDetector.stop();
        }
    }

    @Override
    public void onSignificantMotionDetected() {
        Crashlytics.log(Log.DEBUG, TAG, "Significant motion detected");
        try {
            Session newSession = sessionHelper.createAndReplacePending(false);
            // TODO do not change the meaning of DetectedActivity.UNKNOWN (temporal solution for handling sessions without activities)
            activityHelper.create(DetectedActivity.UNKNOWN, newSession);
            setCurrentSession(newSession);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        new MovementNotification().createNotification(getApplicationContext(), MOTION_NOTIFICATION_ID);
    }

    private class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {
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

//                    SessionHelper sessionHelper = getSessionHelper();
//                    ActivityHelper activityHelper = getActivityHelper();

                    try {
//                        getting last activity, if no, reamins null
                        Activity lastActivity = null;
                        try {
                            lastActivity = activityHelper.getLast();
                        } catch (NullPointerException e) {
                            Crashlytics.log(Log.DEBUG, TAG, "There is no last activity");
                        }

//                        getting last session, if no, remains null
                        Session pendingSession = null;
                        try {
                            pendingSession = sessionHelper.getPending();
                        } catch (NullPointerException e) {
                            Crashlytics.log(Log.DEBUG, TAG, "There is no pending session");
                        }

                        if (newActivityTransitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                            Crashlytics.log(Log.DEBUG, TAG, "New activity has started");

                            if (pendingSession == null) {
//                                no session yet, creating first one
                                pendingSession = createNewSession(newActivityType);
                            } else if (isNewSessionRequired(newActivityType, lastActivity)) {
//                                session types do not match, new session created
                                Crashlytics.log(Log.DEBUG, TAG, "New session is required");
                                handleSignificantMotion(newActivityType);
                                sessionHelper.end(pendingSession);
                                Crashlytics.log(Log.DEBUG, TAG, "Pending session closed");
                                pendingSession = createNewSession(newActivityType);
                            }

                            if (lastActivity.getType() == DetectedActivity.UNKNOWN && sessionHelper.getSessionType(newActivityType) == SessionType.ACTIVE) {
//                                SIGMOV produces UNKNOWN activity type
//                                if following acitivity is active, change it
                                Crashlytics.log(Log.DEBUG, TAG, "Updating last unknown activity");
                                lastActivity.setType(newActivityType);
                                activityHelper.update(lastActivity);
                                // dismiss notification for sensing if the activity is real
                                notificationManager.cancel(MOTION_NOTIFICATION_ID);
                            }

//                            if (isNewSessionRequired(newActivityType, lastActivity)) {
//                                Crashlytics.log(Log.DEBUG, TAG, "New session is required");
//
//                                handleSignificantMotion(newActivityType);
//                                if (pendingSession != null) {
//                                    sessionHelper.end(pendingSession);
//                                    Crashlytics.log(Log.DEBUG, TAG, "Pending session closed");
//                                }
//                                pendingSession = createNewSession(newActivityType);
//                            } else {
//                                Crashlytics.log(Log.DEBUG, TAG, "New session is not required");
//
//                                if (pendingSession == null) {
//                                    pendingSession = createNewSession(newActivityType);
//                                } else if (lastActivity.getType() == DetectedActivity.UNKNOWN) {
//                                    Crashlytics.log(Log.DEBUG, TAG, "Updating last unknown activity");
//                                    lastActivity.setType(newActivityType);
//                                    activityHelper.update(lastActivity);
//                                }
//                            }

//                            add new activity to database with coresponding session
                            activityHelper.create(newActivityType, pendingSession);
                            Crashlytics.log(Log.DEBUG, TAG, "New activity created");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private Session createNewSession(int detectedActivity) throws SQLException {
            Session pendingSession = sessionHelper.create(detectedActivity);
            setCurrentSession(pendingSession);
            Crashlytics.log(Log.DEBUG, TAG, "New session created");
            return pendingSession;
        }

        @Contract("_, null -> true")
        /**
         * returns true, if activity does NOT match or is null
         */
        private boolean isNewSessionRequired(int detectedActivityType, Activity lastActivity) {
            if (lastActivity == null) {
                return true;
            }

            return sessionHelper.getSessionType(detectedActivityType) != sessionHelper.getSessionType(lastActivity);

//            // IN_VEHICLE handling
//            if ((detectedActivityType == DetectedActivity.IN_VEHICLE &&
//                    lastActivity.getType() != DetectedActivity.IN_VEHICLE) || (
//                    detectedActivityType != DetectedActivity.IN_VEHICLE &&
//                            lastActivity.getType() == DetectedActivity.IN_VEHICLE
//            )) {
//                return true;
//            }
//
//            return (detectedActivityType == DetectedActivity.STILL && lastActivity.getType() != DetectedActivity.STILL)
//                    || (lastActivity.getType() == DetectedActivity.STILL && detectedActivityType != DetectedActivity.STILL);
        }
    }
}
