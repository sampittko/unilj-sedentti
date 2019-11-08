package sk.tuke.ms.sedentti.helper;

public class CommonValues {
    // Intents
    public static final String ACTIVITY_RECOGNITION_COMMAND = "sk.tuke.ms.sedentti.activity.recognition.ACTION_PROCESS_ACTIVITY_TRANSITION";

    // Shared Preferences - App settings
    public static final String APP_SHARED_PREFERENCES = "app_settings";
    public static final String APP_SHARED_PREFERENCES_ACTIVE_SECONDS_LIMIT = "active_limit";
    public static final String APP_SHARED_PREFERENCES_SEDENTARY_SECONDS_LIMIT = "sedentary_limit";

    // Shared Preferences - Profile
    public static final String PROFILE_SHARED_PREFERENCES = "profile";
    public static final String PROFILE_SHARED_PREFERENCES_ACTIVE_ID = "active_id";
    public static final long PROFILE_SHARED_PREFERENCES_ACTIVE_ID_DEFAULT = Integer.valueOf("0");
}
