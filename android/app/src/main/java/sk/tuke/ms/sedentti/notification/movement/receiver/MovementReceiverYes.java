package sk.tuke.ms.sedentti.notification.movement.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import static sk.tuke.ms.sedentti.config.PredefinedValues.NOTIFICATION_MOVEMENT_ACTION_YES;
import static sk.tuke.ms.sedentti.config.PredefinedValues.NOTIFICATION_MOVEMENT_EXTRA_ID;

public class MovementReceiverYes extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(NOTIFICATION_MOVEMENT_ACTION_YES)) {
            if (intent.hasExtra(NOTIFICATION_MOVEMENT_EXTRA_ID)) {
                Bundle bundle = intent.getExtras();
                int notificationID = bundle.getInt(NOTIFICATION_MOVEMENT_EXTRA_ID);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationID);
            }
        }
    }
}
