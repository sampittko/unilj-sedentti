package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import sk.tuke.ms.sedentti.helper.CommonStrings;

public class ProfileHelper {
    public static long getActiveProfileId(@NotNull Context context) {
        SharedPreferences profileShPr = context.getSharedPreferences(CommonStrings.PROFILE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return profileShPr.getLong(CommonStrings.PROFILE_SHARED_PREFERENCES_ACTIVE_ID, CommonStrings.PROFILE_SHARED_PREFERENCES_ACTIVE_ID_DEFAULT);
    }
}
