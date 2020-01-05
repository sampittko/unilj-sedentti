package sk.tuke.ms.sedentti.notification.reminders;

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

public class SecondNotification {

    private static final String CHANNEL_ID = "sk.tuke.ms.sedentti.sedentary2";

    private void checkNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Second Sedentary Reminder";

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
                .setColor(ContextCompat.getColor(context, R.color.colorAccentOther))
                .setWhen(System.currentTimeMillis());

        Intent openingIntent = new Intent(context, MainActivity.class);
        openingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openingPendingIntent = PendingIntent.getActivity(context, 0, openingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(openingPendingIntent);

        builder.setGroupSummary(false);
        builder.setDefaults(Notification.DEFAULT_ALL);

        builder.setContentTitle("Stand up");
        builder.setContentText("Forgot to stand up? Lets do it now!");

        notificationManager.notify(id, builder.build());
    }
}
