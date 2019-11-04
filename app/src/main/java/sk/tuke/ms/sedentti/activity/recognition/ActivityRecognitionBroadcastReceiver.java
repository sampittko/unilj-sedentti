package sk.tuke.ms.sedentti.activity.recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityTransitionResult;

import sk.tuke.ms.sedentti.helper.CommonStrings;

public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && CommonStrings.ACTIVITY_RECOGNITION_COMMAND.equals(intent.getAction())) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult intentResult = ActivityTransitionResult
                        .extractResult(intent);
                // handle activity transition result ...
            }
        }

    }
}
