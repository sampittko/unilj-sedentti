package sk.tuke.ms.sedentti;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;
import sk.tuke.ms.sedentti.activity.recognition.ActivityRecognitionBroadcastReceiver;
import sk.tuke.ms.sedentti.activity.recognition.ActivityRecognitionHandler;
import sk.tuke.ms.sedentti.helper.CommonStrings;

public class ActivityRecognitionService extends Service {

    private static final int NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;
    private ActivityRecognitionHandler activityRecognitionHandler;
    private ActivityRecognitionBroadcastReceiver receiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        registerBroadcast();
        activityRecognitionHandler.startTracking();

        return super.onStartCommand(intent, flags, startId);
    }

    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CommonStrings.ACTIVITY_RECOGNITION_COMMAND);

        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.receiver = new ActivityRecognitionBroadcastReceiver();
        activityRecognitionHandler = new ActivityRecognitionHandler(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        activityRecognitionHandler.stopTracking();
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private Notification createNotification() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String unit = preferences.getString("unit", "0");

        String channelId = "sk.tuke.ms.sedentti";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            od Orea vyssie
            CharSequence channelName = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
//            Nougat a nizsie
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setSmallIcon(R.drawable.icon_all_applogo_white_24dp)
//                .setColor(ContextCompat.getColor(this, R.color.primaryColor))
                .setShowWhen(false);


        Intent openingIntent = new Intent(this, MainActivity.class);

        PendingIntent openingPendingIntent = PendingIntent.getActivity(this, 0, openingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(openingPendingIntent);

        return builder.build();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
