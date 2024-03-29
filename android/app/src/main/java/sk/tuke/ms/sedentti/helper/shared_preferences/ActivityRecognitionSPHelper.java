package sk.tuke.ms.sedentti.helper.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

import sk.tuke.ms.sedentti.config.PredefinedValues;

public class ActivityRecognitionSPHelper {

    private final SharedPreferences preferences;

    public ActivityRecognitionSPHelper(Context context) {
        this.preferences = context.getSharedPreferences(PredefinedValues.ACTIVITY_RECOGNITION_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public int getActivityRecognitionState() {
        return preferences.getInt(PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STATE, PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED);
    }

    public void saveStateToSharedPreferences(int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STATE, value);

        editor.apply();
    }
}
