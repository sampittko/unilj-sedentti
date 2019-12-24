package sk.tuke.ms.sedentti.notification.movement.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.recognition.activity.ActivityRecognitionService;

import static sk.tuke.ms.sedentti.config.PredefinedValues.COMMAND_TURN_ON_SIGMOV;
import static sk.tuke.ms.sedentti.config.PredefinedValues.NOTIFICATION_MOVEMENT_ACTION_NO;
import static sk.tuke.ms.sedentti.config.PredefinedValues.NOTIFICATION_MOVEMENT_EXTRA_ID;

public class MovementReceiverNo extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NOTIFICATION_MOVEMENT_ACTION_NO)) {
            final PendingResult pendingResult = goAsync();
            Task asyncTask = new Task(pendingResult, intent, context);
            asyncTask.execute();
        }
    }

    private static class Task extends AsyncTask<Void, Void, Void> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private final Context context;

        private Task(PendingResult pendingResult, Intent intent, Context context) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (intent.getAction() == null) {
                return null;
            }

            if (intent.getAction().equals(NOTIFICATION_MOVEMENT_ACTION_NO)) {
                if (intent.hasExtra(NOTIFICATION_MOVEMENT_EXTRA_ID)) {
                    Bundle bundle = intent.getExtras();
                    int notificationID = bundle.getInt(NOTIFICATION_MOVEMENT_EXTRA_ID);

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(notificationID);
                    try {
                        ProfileHelper profileHelper = new ProfileHelper(context);
                        Profile profile = profileHelper.getActive();
                        SessionHelper sessionHelper = new SessionHelper(context, profile);

                        sessionHelper.discardPendingAndUndoPrevious();

                        Session session = sessionHelper.getPending();
                        if (session.isSedentary()) {
                            Intent intent = new Intent(context, ActivityRecognitionService.class);
                            intent.setAction(COMMAND_TURN_ON_SIGMOV);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent);
                            } else {
                                context.startService(intent);
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pendingResult.finish();
        }
    }
}
