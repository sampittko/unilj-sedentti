package sk.tuke.ms.sedentti.recognition;

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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.activity.MainActivity;
import sk.tuke.ms.sedentti.helper.ActitivityRecognitionSPHelper;
import sk.tuke.ms.sedentti.helper.CommonValues;

public class ActivityRecognitionService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "sk.tuke.ms.sedentti";

    private NotificationManager notificationManager;
    private ActivityRecognitionHandler activityRecognitionHandler;
    private ActivityRecognitionBroadcastReceiver receiver;
    private ActitivityRecognitionSPHelper activityRecognitionPreferences;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int commandResult = processCommand(intent);
        startForeground(NOTIFICATION_ID, createNotification(commandResult));

        return super.onStartCommand(intent, flags, startId);
    }

    private int processCommand(Intent intent) {
//        if state is unknown, service is only started without sensing
        if (intent == null || intent.getAction() == null) {
            return CommonValues.ACTIVITY_RECOGNITION_SERVICE_UNKNOWN;
        }

        switch (intent.getAction()) {
            case CommonValues.COMMAND_INIT:
                int state = this.activityRecognitionPreferences.getActivityRecognitionState();

                if (state == CommonValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING) {
                    serviceToggle(CommonValues.COMMAND_START);
                } else {
                    serviceToggle(CommonValues.COMMAND_START);
                }

                return state;

            case CommonValues.COMMAND_START:
                serviceToggle(CommonValues.COMMAND_START);
                this.activityRecognitionPreferences.saveStateToSharedPreferences(CommonValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING);

                return CommonValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING;

            case CommonValues.COMMAND_STOP:
                serviceToggle(CommonValues.COMMAND_STOP);
                this.activityRecognitionPreferences.saveStateToSharedPreferences(CommonValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED);

                return CommonValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED;
        }
//        Log.i(TAG, "Actual state is: " + state);7
        return CommonValues.ACTIVITY_RECOGNITION_SERVICE_UNKNOWN;
    }

    private void serviceToggle(String command) {
        if (command.equals(CommonValues.COMMAND_START)) {
            registerReceiver(receiver, new IntentFilter(CommonValues.ACTIVITY_RECOGNITION_COMMAND));
            this.activityRecognitionHandler.startTracking();
        } else if (command.equals(CommonValues.COMMAND_STOP)) {
            this.activityRecognitionHandler.stopTracking();
            unregisterReceiver(receiver);
        }
    }





    @Override
    public void onCreate() {
        super.onCreate();

        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.receiver = new ActivityRecognitionBroadcastReceiver();
        activityRecognitionHandler = new ActivityRecognitionHandler(getApplicationContext());
        this.activityRecognitionPreferences = new ActitivityRecognitionSPHelper(getApplicationContext());
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

        String state = null;
        if (commandResult == CommonValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING) {
            state = "Sedentti is tracking your sitting";
        } else if (commandResult == CommonValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED) {
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
