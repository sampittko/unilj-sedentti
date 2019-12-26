package sk.tuke.ms.sedentti.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.activity.MainActivity;
import sk.tuke.ms.sedentti.config.PredefinedValues;

public class ServiceNotification {

    private static final String CHANNEL_ID = "sk.tuke.ms.sedentti.service";

    private void checkNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Service Notification";

            if (notificationManager != null) {
                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);

                if (notificationChannel == null) {
                    notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }
    }

    public Notification createNotification(Context context, int commandResult, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        checkNotificationChannel(notificationManager);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_logo_shape_white_24dx)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setShowWhen(false);


        Intent openingIntent = new Intent(context, MainActivity.class);
        openingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openingPendingIntent = PendingIntent.getActivity(context, 0, openingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
}
