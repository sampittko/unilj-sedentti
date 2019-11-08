package sk.tuke.ms.sedentti.helper;

import android.content.Context;
import android.content.SharedPreferences;

import sk.tuke.ms.sedentti.model.Profile;

public class SharedPreferencesHelper {
    private SharedPreferences profileSharedPreferences;

    public SharedPreferencesHelper(Context context) {
        profileSharedPreferences = context.getSharedPreferences(CommonValues.PROFILE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void updateActiveProfile(Profile profile) {
        SharedPreferences.Editor profileShPrEditor = profileSharedPreferences.edit();
        profileShPrEditor.putLong(CommonValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID, profile.getId());
        profileShPrEditor.apply();
    }
}
