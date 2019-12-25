package sk.tuke.ms.sedentti.config;

/**
 * List of contants not suited for changes
 */
public abstract class PredefinedValues {
    //
    // Intents
    //
    public static final String ACTIVITY_RECOGNITION_COMMAND =
            "sk.tuke.ms.sedentti.activity.recognition.ACTION_PROCESS_ACTIVITY_TRANSITION";
    public static final String COMMAND_START = "sk.tuke.ms.sedentti.service.activity.recognition.ACTION_START";
    public static final String COMMAND_STOP = "sk.tuke.ms.sedentti.service.activity.recognition.ACTION_STOP";
    public static final String COMMAND_STOP_AND_SAVE = "sk.tuke.ms.sedentti.service.activity.recognition.ACTION_STOP_AND_SAVE";
    public static final String COMMAND_INIT = "sk.tuke.ms.sedentti.service.activity.recognition.ACTION_INIT";
    public static final String COMMAND_TURN_ON_SIGMOV = "sk.tuke.ms.sedentti.service.activity.recognition.ACTION_TURN_ON_SIGMOV";
    public static final String ALARM_STOP_SERVICE = "sk.tuke.ms.sedentti.alarm.receiver.ACTION_STOP_AND_SAVE";

    //
    // Notification
    //
    public static final String NOTIFICATION_MOVEMENT_ACTION_YES = "sk.tuke.ms.sedentti.notification.receiver.ACTION_YES";
    public static final String NOTIFICATION_MOVEMENT_ACTION_NO = "sk.tuke.ms.sedentti.notification.receiver.ACTION_NO";
    public static final String NOTIFICATION_MOVEMENT_EXTRA_ID = "sk.tuke.ms.sedentti.notification.EXTRA_ID";

    //
    // Shared Preferences - App settings
    //
    public static final String APP_SHARED_PREFERENCES = Configuration.APP_PACKAGE + "_preferences";
    public static final String APP_SHARED_PREFERENCES_ACTIVE_LIMIT = "active_limit";
    public static final String APP_SHARED_PREFERENCES_SEDENTARY_LIMIT = "sedentary_limit";
    public static final String APP_SHARED_PREFERENCES_SIG_MOV_NOTIF_STATE = "sig_mov_notif_state";
    public static final String APP_SHARED_PREFERENCES_FIRST_NOTIF_STATE = "first_notif_state";
    public static final String APP_SHARED_PREFERENCES_FIRST_NOTIF_TIME = "first_notif_time";
    public static final String APP_SHARED_PREFERENCES_SECOND_NOTIF_STATE = "second_notif_state";
    public static final String APP_SHARED_PREFERENCES_SYNC_INTERVAL = "sync_interval";
    public static final String APP_SHARED_PREFERENCES_FIRST_TIME_STARTUP_PERFORMED = "first_time_startup_performed";
    public static final String APP_SHARED_PREFERENCES_STOP_SENSING_RELATIVE_VALUE = "stop_sensing_relative_value";


    //
    // Shared Preferences - Profile
    //
    public static final String PROFILE_SHARED_PREFERENCES= Configuration.APP_PACKAGE + "_preferences.profile";
    public static final String PROFILE_SHARED_PREFERENCES_ACTIVE_ID = "active_id";
    public static final long PROFILE_SHARED_PREFERENCES_ACTIVE_ID_DEFAULT = 0L;

    //
    // Shared Preferences - Activity Recognition
    //
    public static final String ACTIVITY_RECOGNITION_SHARED_PREFERENCES= Configuration.APP_PACKAGE + "_preferences.activity_recognition_settings";
    public static final String ACTIVITY_RECOGNITION_SERVICE_STATE = "activity_recognition_service_state";
    public static final int ACTIVITY_RECOGNITION_SERVICE_RUNNING = 1;
    public static final int ACTIVITY_RECOGNITION_SERVICE_STOPPED = 0;
    public static final int ACTIVITY_RECOGNITION_SERVICE_UNKNOWN = -1;

    //
    // Codes
    //
    public static final int FIREBASE_CODE_SIGN_IN = 1;
    public static final int PERMISSION_REQUEST_CODE_READ_WRITE_EXTERNAL_STORAGE = 1;

    //
    // Firebase Cloud Storage
    //
    public static final String CLOUD_STORAGE_FOLDER_SEPARATOR = "/";

    //
    // DatabaseExporter
    //
    public static final String DB_EXPORTER_CSV_DATA_SEPARATOR = ",";
    public static final String DB_EXPORTER_CSV_INNER_DATA_SEPARATOR = "-";
}
