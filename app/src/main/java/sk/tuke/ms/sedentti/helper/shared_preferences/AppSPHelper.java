package sk.tuke.ms.sedentti.helper.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.config.Settings;

public class AppSPHelper {
    private SharedPreferences appSharedPreferences;

    public AppSPHelper(@NotNull Context context) {
        appSharedPreferences = context.getSharedPreferences(PredefinedValues.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Sets default settings for the app
     */
    public void setAppDefaultSettings() {
        updateActiveLimit();
        updateSedentaryLimit();
    }

    /**
     * Updates sedentary seconds limit to default value
     */
    public void updateSedentaryLimit() {
        updateSedentaryLimit(Settings.SEDENTARY_MILLISECONDS_LIMIT);
    }

    /**
     * @param value Sedentary seconds limit value to set
     */
    public void updateSedentaryLimit(int value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT, value);
    }

    /**
     * Updates active seconds limit to default value
     */
    public void updateActiveLimit() {
        updateActiveLimit(Settings.ACTIVE_MILLISECONDS_LIMIT);
    }

    /**
     * @param value Active seconds limit value to set
     */
    public void updateActiveLimit(int value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_LIMIT, value);
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
    public int getSedentaryLimit() {
        return appSharedPreferences.getInt(
                PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT,
                PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT_DEFAULT
        );
    }

    /**
     * @return Active seconds limit
     */
    public int getActiveLimit() {
        return appSharedPreferences.getInt(
                PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_LIMIT,
                PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_LIMIT_DEFAULT
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
