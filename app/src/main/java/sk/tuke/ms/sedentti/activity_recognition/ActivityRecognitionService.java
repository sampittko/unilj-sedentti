package sk.tuke.ms.sedentti.activity_recognition;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.activity.MainActivity;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.helper.shared_preferences.ActivityRecognitionSPHelper;

public class ActivityRecognitionService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "sk.tuke.ms.sedentti";
    private static final String TAG = "ActivityRecognitionS";

    private NotificationManager notificationManager;
    private ActivityRecognitionHandler activityRecognitionHandler;
    private ActivityRecognitionBroadcastReceiver receiver;
    private ActivityRecognitionSPHelper activityRecognitionPreferences;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int commandResult = processCommand(intent);
        startForeground(NOTIFICATION_ID, createNotification(commandResult));

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
                    // register receiver just avoid the case when there is no registered register
                    registerReceiver(receiver, new IntentFilter(PredefinedValues.ACTIVITY_RECOGNITION_COMMAND));
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
            Log.i(TAG, "Sensing service started");
        } else if (command.equals(PredefinedValues.COMMAND_STOP)) {
            this.activityRecognitionHandler.stopTracking();
            unregisterReceiver(receiver);
            Log.i(TAG, "Sensing service stopped");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.receiver = new ActivityRecognitionBroadcastReceiver();
        activityRecognitionHandler = new ActivityRecognitionHandler(getApplicationContext());
        this.activityRecognitionPreferences = new ActivityRecognitionSPHelper(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        activityRecognitionHandler.stopTracking();
        unregisterReceiver(receiver);
        super.onDestroy();
    }


    private Notification createNotification(int commandResult) {

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

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ActivityRecognitionService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ActivityRecognitionService.this;
        }

    }

}
