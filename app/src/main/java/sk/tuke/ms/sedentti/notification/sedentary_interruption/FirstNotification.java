package sk.tuke.ms.sedentti.notification.sedentary_interruption;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import sk.tuke.ms.sedentti.R;

public class FirstNotification {

    private static final String CHANNEL_ID = "sk.tuke.ms.sedentti.sedentary1";

    private void checkNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Sedentary Notification 1";

            if (notificationManager != null) {
                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);

                if (notificationChannel == null) {
                    notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
                notificationChannel.enableVibration(true);
                notificationChannel.enableLights(false);
            }
        }
    }

    public void createNotification(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        checkNotificationChannel(notificationManager);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_logo_shape_white_24dx)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setWhen(System.currentTimeMillis());

        builder.setGroupSummary(false);
        builder.setDefaults(Notification.DEFAULT_ALL);

        builder.setContentTitle("Stand up");
        builder.setContentText("Get your blood flowing for a bit!");

        notificationManager.notify(id, builder.build());
    }
}
