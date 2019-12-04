package sk.tuke.ms.sedentti.helper;

import sk.tuke.ms.sedentti.config.DefaultSettings;

public class CommonValues {
    // Intents
    public static final String ACTIVITY_RECOGNITION_COMMAND =
            "sk.tuke.ms.sedentti.activity.recognition.ACTION_PROCESS_ACTIVITY_TRANSITION";

    // Shared Preferences - App settings
    public static final String APP_SHARED_PREFERENCES = "app_settings";

    public static final String APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT = "active_limit";
    public static final int APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT_DEFAULT = DefaultSettings.ACTIVE_MILLISECONDS_LIMIT;

    public static final String APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT = "sedentary_limit";
    public static final int APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT_DEFAULT = DefaultSettings.SEDENTARY_MILLISECONDS_LIMIT;

    public static final String APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED = "first_time_startup_performed";
    public static final boolean APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED_DEFAULT = DefaultSettings.FIRST_TIME_STARTUP_PERFORMED;

    // Shared Preferences - Profile
    public static final String PROFILE_SHARED_PREFERENCES = "profile";

    public static final String PROFILE_SHARED_PREFERENCES_ACTIVE_ID = "active_id";
    public static final long PROFILE_SHARED_PREFERENCES_ACTIVE_ID_DEFAULT = 0L;

    // Firebase Codes
    public static final int RC_SIGN_IN = 1;
}
