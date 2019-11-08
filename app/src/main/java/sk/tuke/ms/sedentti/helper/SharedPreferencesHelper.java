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

    /**
     * @param profile Profile to update
     */
    public void updateActiveProfile(Profile profile) {
        SharedPreferences.Editor profileShPrEditor = profileSharedPreferences.edit();

        profileShPrEditor.putLong(
                CommonValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID,
                profile.getId()
        );

        profileShPrEditor.apply();
    }

    /**
     * @return ID of currently active profile
     */
    public long getActiveProfileId() {
        return profileSharedPreferences.getLong(
                CommonValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID,
                CommonValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID_DEFAULT
        );
    }

    /**
     * Sets default settings for the app
     */
    public void setAppDefaultSettings() {
        updateActiveSecondsLimit();
        updateSedentarySecondsLimit();
    }

    /**
     * Updates sedentary seconds limit to default value
     */
    public void updateSedentarySecondsLimit() {
        updateSedentarySecondsLimit(DefaultSettings.SEDENTARY_SECONDS_LIMIT);
    }

    /**
     * @param value Sedentary seconds limit value to set
     */
    public void updateSedentarySecondsLimit(int value) {
        updateAppSetting(CommonValues.APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT, value);
    }

    /**
     * Updates active seconds limit to default value
     */
    public void updateActiveSecondsLimit() {
        updateActiveSecondsLimit(DefaultSettings.ACTIVE_SECONDS_LIMIT);
    }

    /**
     * @param value Active seconds limit value to set
     */
    public void updateActiveSecondsLimit(int value) {
        updateAppSetting(CommonValues.APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT, value);
    }

    private void updateAppSetting(String setting, int value) {
        SharedPreferences.Editor appShPrEditor = appSharedPreferences.edit();
        appShPrEditor.putInt(setting, value);
        appShPrEditor.apply();
    }

    /**
     * @return Sedentary seconds limit
     */
    public int getSedentarySecondsLimit() {
        return appSharedPreferences.getInt(
                CommonValues.APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT,
                CommonValues.APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT_DEFAULT
        );
    }

    /**
     * @return Active seconds limit
     */
    public int getActiveSecondsLimit() {
        return appSharedPreferences.getInt(
                CommonValues.APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT,
                CommonValues.APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT_DEFAULT
        );
    }
}
