package sk.tuke.ms.sedentti.recognition.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import static sk.tuke.ms.sedentti.config.PredefinedValues.ALARM_STOP_SERVICE;
import static sk.tuke.ms.sedentti.config.PredefinedValues.COMMAND_STOP_AND_SAVE;

public class ActivityRecognitionStopReceiver extends BroadcastReceiver {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(ALARM_STOP_SERVICE)) {
            Intent serviceIntent = new Intent(context, ActivityRecognitionService.class);
            serviceIntent.setAction(COMMAND_STOP_AND_SAVE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

            Crashlytics.log(Log.DEBUG, TAG, "Activity recognition foreground service started");
        }
    }
}
