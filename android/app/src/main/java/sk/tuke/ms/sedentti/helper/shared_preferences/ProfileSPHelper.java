package sk.tuke.ms.sedentti.helper.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.model.Profile;

public class ProfileSPHelper {
    private SharedPreferences profileSharedPreferences;

    public ProfileSPHelper(@NotNull Context context) {
        profileSharedPreferences = context.getSharedPreferences(PredefinedValues.PROFILE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * @param profile Profile to update
     */
    public void updateActiveProfile(@NotNull Profile profile) {
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
}
