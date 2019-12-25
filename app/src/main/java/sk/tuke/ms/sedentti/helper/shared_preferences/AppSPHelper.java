package sk.tuke.ms.sedentti.helper.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.config.PredefinedValues;

public class AppSPHelper {
    private SharedPreferences appSharedPreferences;

    public AppSPHelper(@NotNull Context context) {
        appSharedPreferences = context.getSharedPreferences(PredefinedValues.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Sets default settings for the app
     */
    public void setAppDefaultSettings() {
        setDefaultActiveLimit();
        setDefaultSedentaryLimit();
        setDefaultSigMovNotifState();
        setDefaultFirstNotifState();
        setDefaultFirstNotifTime();
        setDefaultSecondNotifState();
        setDefaultSyncInterval();
        setDefaultFirstTimeStartupPerformed();
    }

    private void setDefaultActiveLimit() {
        setActiveLimit(Configuration.APP_SHARED_PREFERENCES_ACTIVE_LIMIT_DEFAULT);
    }

    private void setDefaultSedentaryLimit() {
        setSedentaryLimit(Configuration.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT_DEFAULT);
    }

    private void setDefaultSigMovNotifState() {
        setSigMovNotifState(Configuration.APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE_DEFAULT);
    }

    private void setDefaultFirstNotifState() {
        setFirstNotifState(Configuration.APP_SHARED_PREFERENCES_FIRST_NOTIF_STATE_DEFAULT);
    }

    private void setDefaultFirstNotifTime() {
        setFirstNotifTime(Configuration.APP_SHARED_PREFERENCES_FIRST_NOTIF_TIME_DEFAULT);
    }

    private void setDefaultSecondNotifState() {
        setSecondNotifState(Configuration.APP_SHARED_PREFERENCES_SECOND_NOTIF_STATE_DEFAULT);
    }

    private void setDefaultSyncInterval() {
        setSyncInterval(Configuration.APP_SHARED_PREFERENCES_SYNC_INTERVAL_DEFAULT);
    }

    private void setDefaultFirstTimeStartupPerformed() {
        setFirstTimeStartupPerformed(Configuration.APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED_DEFAULT);
    }

    /**
     * @param value
     */
    public void setSedentaryLimit(int value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT, value);
    }

    /**
     * @param value
     */
    public void setActiveLimit(int value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_LIMIT, value);
    }

    /**
     * @param value
     */
    public void setSigMovNotifState(boolean value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE, value);
    }

    /**
     * @param value
     */
    public void setFirstNotifState(boolean value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_FIRST_NOTIF_STATE, value);
    }

    /**
     * @param value
     */
    public void setFirstNotifTime(int value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_FIRST_NOTIF_TIME, value);
    }

    /**
     * @param value
     */
    public void setSecondNotifState(boolean value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_SECOND_NOTIF_STATE, value);
    }

    /**
     * @param value
     */
    public void setSyncInterval(int value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_SYNC_INTERVAL, value);
    }

    /**
     * @param value
     */
    public void setFirstTimeStartupPerformed(boolean value) {
        updateAppSetting(PredefinedValues.APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED, value);
    }

    /**
     * @return
     */
    public int getSedentaryLimit() {
        return appSharedPreferences.getInt(
                PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT,
                Configuration.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT_DEFAULT
        );
    }

    /**
     * @return
     */
    public int getActiveLimit() {
        return appSharedPreferences.getInt(
                PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_LIMIT,
                Configuration.APP_SHARED_PREFERENCES_ACTIVE_LIMIT_DEFAULT
        );
    }

    /**
     * @return
     */
    public boolean getSigMovNotifState() {
        return appSharedPreferences.getBoolean(
                PredefinedValues.APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE,
                Configuration.APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE_DEFAULT
        );
    }

    /**
     * @return
     */
    public boolean getFirstNotifState() {
        return appSharedPreferences.getBoolean(
                PredefinedValues.APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE,
                Configuration.APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE_DEFAULT
        );
    }

    /**
     * @return
     */
    public int getFirstNotifTime() {
        return appSharedPreferences.getInt(
                PredefinedValues.APP_SHARED_PREFERENCES_FIRST_NOTIF_TIME,
                Configuration.APP_SHARED_PREFERENCES_FIRST_NOTIF_TIME_DEFAULT
        );
    }

    /**
     * @return
     */
    public boolean getSecondNotifState() {
        return appSharedPreferences.getBoolean(
                PredefinedValues.APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE,
                Configuration.APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE_DEFAULT
        );
    }

    /**
     * @return
     */
    public int getSyncInterval() {
        return appSharedPreferences.getInt(
                PredefinedValues.APP_SHARED_PREFERENCES_SYNC_INTERVAL,
                Configuration.APP_SHARED_PREFERENCES_SYNC_INTERVAL_DEFAULT
        );
    }

    /**
     * @return
     */
    public boolean getFirstTimeStartupPerformed() {
        return appSharedPreferences.getBoolean(
                PredefinedValues.APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED,
                Configuration.APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED_DEFAULT
        );
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
}
