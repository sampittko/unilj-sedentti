package sk.tuke.ms.sedentti.helper;

import android.content.Context;
import android.content.SharedPreferences;

import sk.tuke.ms.sedentti.config.DefaultSettings;
import sk.tuke.ms.sedentti.model.Profile;

public class SharedPreferencesHelper {
    private SharedPreferences profileSharedPreferences;
    private SharedPreferences appSharedPreferences;

    public SharedPreferencesHelper(Context context) {
        profileSharedPreferences = context.getSharedPreferences(CommonValues.PROFILE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        appSharedPreferences = context.getSharedPreferences(CommonValues.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void updateActiveProfile(Profile profile) {
        SharedPreferences.Editor profileShPrEditor = profileSharedPreferences.edit();
        profileShPrEditor.putLong(CommonValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID, profile.getId());
        profileShPrEditor.apply();
    }

    public long getActiveProfileId() {
        return profileSharedPreferences.getLong(CommonValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID, CommonValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID_DEFAULT);
    }

    public void updateAppSettings() {
        updateActiveSecondsLimit();
        updateSedentarySecondsLimit();
    }

    public void updateSedentarySecondsLimit() {
        SharedPreferences.Editor appShPrEditor = appSharedPreferences.edit();
        appShPrEditor.putInt(CommonValues.APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT, DefaultSettings.SEDENTARY_SECONDS_LIMIT);
        appShPrEditor.apply();
    }

    public void updateActiveSecondsLimit() {
        SharedPreferences.Editor appShPrEditor = appSharedPreferences.edit();
        appShPrEditor.putInt(CommonValues.APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT, DefaultSettings.ACTIVE_SECONDS_LIMIT);
        appShPrEditor.apply();
    }
}
