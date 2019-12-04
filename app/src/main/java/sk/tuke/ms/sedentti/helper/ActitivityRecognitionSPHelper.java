package sk.tuke.ms.sedentti.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class ActitivityRecognitionSPHelper {

    private final SharedPreferences preferences;

    public ActitivityRecognitionSPHelper(Context context) {
        this.preferences = context.getSharedPreferences(CommonValues.ACTIVITY_RECOGNITION_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public int getActivityRecognitionState() {
        return preferences.getInt(CommonValues.ACTIVITY_RECOGNITION_SERVICE_STATE, CommonValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED);
    }

    public void saveStateToSharedPreferences(int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(CommonValues.ACTIVITY_RECOGNITION_SERVICE_STATE, value);

        editor.apply();
    }
}
