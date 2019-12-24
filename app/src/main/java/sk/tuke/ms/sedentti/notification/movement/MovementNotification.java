package sk.tuke.ms.sedentti.notification.movement;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

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
            CharSequence channelName = "Sensing Notification";

            if (notificationManager != null) {
                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);

                if (notificationChannel == null) {
                    notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
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
                .setSmallIcon(R.drawable.ic_person_outline_black_24dp)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setWhen(System.currentTimeMillis());

        builder.setVibrate(new long[]{1000, 1000});
        builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        builder.setContentTitle("Movement");
        builder.setContentText("We've detected movement, is that right?");

        builder.addAction(R.drawable.ic_done_black_24dp, "Yes", yesPendingIntent);
        builder.addAction(R.drawable.ic_close_black_24dp, "No, discard", noPendingIntent);

        notificationManager.notify(id, builder.build());
    }
}
