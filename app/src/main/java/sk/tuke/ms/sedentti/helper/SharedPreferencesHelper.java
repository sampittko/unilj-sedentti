package sk.tuke.ms.sedentti.helper;

import android.content.Context;
import android.content.SharedPreferences;

import sk.tuke.ms.sedentti.config.Settings;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.model.Profile;

public class SharedPreferencesHelper {
    private SharedPreferences profileSharedPreferences;
    private SharedPreferences appSharedPreferences;

    public SharedPreferencesHelper(Context context) {
        profileSharedPreferences = context.getSharedPreferences(PredefinedValues.PROFILE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        appSharedPreferences = context.getSharedPreferences(PredefinedValues.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * @param profile Profile to update
     */
    public void updateActiveProfile(Profile profile) {
        SharedPreferences.Editor profileShPrEditor = profileSharedPreferences.edit();

        profileShPrEditor.putLong(
                PredefinedValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID,
                profile.getId()
        );

        profileShPrEditor.apply();
    }

    /**
     * @return ID of currently active profile
     */
    public long getActiveProfileId() {
        return profileSharedPreferences.getLong(
                PredefinedValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID,
                PredefinedValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID_DEFAULT
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
        updateSedentarySecondsLimit(Settings.SEDENTARY_MILLISECONDS_LIMIT);
    }

    /**
     * @param value Sedentary seconds limit value to set
     */
    public void updateSedentarySecondsLimit(int value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT, value);
    }

    /**
     * Updates active seconds limit to default value
     */
    public void updateActiveSecondsLimit() {
        updateActiveSecondsLimit(Settings.ACTIVE_MILLISECONDS_LIMIT);
    }

    /**
     * @param value Active seconds limit value to set
     */
    public void updateActiveSecondsLimit(int value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT, value);
    }

    private void updateAppSetting(String setting, int value) {
        SharedPreferences.Editor appShPrEditor = appSharedPreferences.edit();
        appShPrEditor.putInt(setting, value);
        appShPrEditor.apply();
    }

    private void updateAppSetting(String setting, boolean value) {
        SharedPreferences.Editor appShPrEditor = appSharedPreferences.edit();
        appShPrEditor.putBoolean(setting, value);
        appShPrEditor.apply();
    }

    /**
     * @return Sedentary seconds limit
     */
    public int getSedentarySecondsLimit() {
        return appSharedPreferences.getInt(
                PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT,
                PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT_DEFAULT
        );
    }

    /**
     * @return Active seconds limit
     */
    public int getActiveSecondsLimit() {
        return appSharedPreferences.getInt(
                PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT,
                PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT_DEFAULT
        );
    }

    /**
     * @return First time startup performed (true / false)
     */
    public boolean firstTimeStartupPerformed() {
        return appSharedPreferences.getBoolean(
                PredefinedValues.APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED,
                PredefinedValues.APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED_DEFAULT
        );
    }

    /**
     * @param value Value to set
     */
    public void updateFirstTimeStartupPerformed(boolean value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED, value);
    }
}
