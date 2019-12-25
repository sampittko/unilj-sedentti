package sk.tuke.ms.sedentti.helper.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.config.PredefinedValues;

public class AppSPHelper {
    private SharedPreferences appSharedPreferences;

    public AppSPHelper(@NotNull Context context) {
        appSharedPreferences = context.getSharedPreferences(PredefinedValues.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE);
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
        return Integer.valueOf(
                    Objects.requireNonNull(
                            appSharedPreferences.getString(
                            PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT,
                            Configuration.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT_DEFAULT
                    )
                )
        );
    }

    /**
     * @return
     */
    public int getActiveLimit() {
        return Integer.valueOf(
                Objects.requireNonNull(
                        appSharedPreferences.getString(
                            PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_LIMIT,
                            Configuration.APP_SHARED_PREFERENCES_ACTIVE_LIMIT_DEFAULT
                        )
                )
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
                Configuration.APP_SHARED_PREFERENCES_FIRST_NOTIF_STATE_DEFAULT
        );
    }

    /**
     * @return
     */
    public int getFirstNotifTime() {
        return Integer.valueOf(
                Objects.requireNonNull(
                        appSharedPreferences.getString(
                            PredefinedValues.APP_SHARED_PREFERENCES_FIRST_NOTIF_TIME,
                            Configuration.APP_SHARED_PREFERENCES_FIRST_NOTIF_TIME_DEFAULT
                        )
                )
        );
    }

    /**
     * @return
     */
    public boolean getSecondNotifState() {
        return appSharedPreferences.getBoolean(
                PredefinedValues.APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE,
                Configuration.APP_SHARED_PREFERENCES_SECOND_NOTIF_STATE_DEFAULT
        );
    }

    /**
     * @return
     */
    public int getSyncInterval() {
        return Integer.valueOf(
                Objects.requireNonNull(
                        appSharedPreferences.getString(
                            PredefinedValues.APP_SHARED_PREFERENCES_SYNC_INTERVAL,
                            Configuration.APP_SHARED_PREFERENCES_SYNC_INTERVAL_DEFAULT
                        )
                )
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
        appShPrEditor.putString(setting, String.valueOf(value));
        appShPrEditor.apply();
    }

    private void updateAppSetting(String setting, boolean value) {
        SharedPreferences.Editor appShPrEditor = appSharedPreferences.edit();
        appShPrEditor.putBoolean(setting, value);
        appShPrEditor.apply();
    }
}
