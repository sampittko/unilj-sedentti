package sk.tuke.ms.sedentti.notification.movement;

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
import sk.tuke.ms.sedentti.notification.movement.receiver.MovementReceiverNo;
import sk.tuke.ms.sedentti.notification.movement.receiver.MovementReceiverYes;

import static sk.tuke.ms.sedentti.config.PredefinedValues.NOTIFICATION_MOVEMENT_ACTION_NO;
import static sk.tuke.ms.sedentti.config.PredefinedValues.NOTIFICATION_MOVEMENT_ACTION_YES;
import static sk.tuke.ms.sedentti.config.PredefinedValues.NOTIFICATION_MOVEMENT_EXTRA_ID;

public class MovementNotification {

    private static final String CHANNEL_ID = "sk.tuke.ms.sedentti.sensing";

    private void checkNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Significant Movement Notification";

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

        Intent yesIntent = new Intent(context, MovementReceiverYes.class);
        yesIntent.setAction(NOTIFICATION_MOVEMENT_ACTION_YES);
        yesIntent.putExtra(NOTIFICATION_MOVEMENT_EXTRA_ID, id);
        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(context, 0, yesIntent, 0);

        Intent noIntent = new Intent(context, MovementReceiverNo.class);
        noIntent.setAction(NOTIFICATION_MOVEMENT_ACTION_NO);
        noIntent.putExtra(NOTIFICATION_MOVEMENT_EXTRA_ID, id);
        PendingIntent noPendingIntent = PendingIntent.getBroadcast(context, 0, noIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_logo_shape_white_24dx)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setWhen(System.currentTimeMillis());

        builder.setGroupSummary(false);
        builder.setDefaults(Notification.DEFAULT_ALL);

        builder.setContentTitle("Movement");
        builder.setContentText("We've detected movement, is that right?");

        builder.addAction(R.drawable.ic_done_black_24dp, "Yes", yesPendingIntent);
        builder.addAction(R.drawable.ic_close_black_24dp, "No, discard", noPendingIntent);

        notificationManager.notify(id, builder.build());
    }
}
