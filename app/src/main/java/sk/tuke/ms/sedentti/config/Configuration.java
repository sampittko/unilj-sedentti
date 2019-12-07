package sk.tuke.ms.sedentti.config;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;

/**
 * List of contants suited for changes by the developer
 */
public abstract class Configuration {
    public static final boolean FIRST_TIME_STARTUP_PERFORMED = false;

    //
    // Firebase Authentication
    //
    public static final String PROFILE_UNKNOWN_DISPLAY_NAME = "Unknown Name";
    public static final String PROFILE_UNKNOWN_EMAIL = "";
    public static final String PROFILE_UNKNOWN_PHOTO_URL = "";

    //
    // Firebase Cloud Storage
    //
    public static final String STORAGE_DATE_PATH_SEPARATOR = "-";
    public static final String STORAGE_FILE_TYPE = ".csv";

    //
    // Local Database
    //
    public static final String DATABASE_NAME = "sedentti";
    public static final int DATABASE_VERSION = 5;

    //
    // Upload work
    //
    public static final NetworkType UPLOAD_WORK_NETWORK_TYPE = NetworkType.CONNECTED;
    public static final String UPLOAD_WORK_NAME = "UPLOAD_WORK";
    public static final ExistingPeriodicWorkPolicy UPLOAD_WORK_POLICY = ExistingPeriodicWorkPolicy.KEEP;
    public static final int UPLOAD_WORK_RESULT_WAITING_THREAD_SLEEP_MILLISECONDS_LENGTH = 1000;
    public static final int UPLOAD_WORK_MINUTES_UPLOAD_INTERVAL = 10;
    public static final String UPLOAD_WORK_UNDO_REASON = "Upload task was canceled";

    //
    // DatabaseExporter
    //
    public static final String CSV_EXPORT_FILENAME = "export" + STORAGE_FILE_TYPE;
    public static final String CSV_HEADER_COLUMN_1 = "USER";
    public static final String CSV_HEADER_COLUMN_2 = "SESSION";
    public static final String CSV_HEADER_COLUMN_3 = "SESSION_SEDENTARY";
    public static final String CSV_HEADER_COLUMN_4 = "SESSION_START_TIMESTAMP";
    public static final String CSV_HEADER_COLUMN_5 = "SESSION_END_TIMESTAMP";
    public static final String CSV_HEADER_COLUMN_6 = "SESSION_DURATION";
    public static final String CSV_HEADER_COLUMN_7 = "SESSION_SUCCESSFUL";
    public static final String CSV_HEADER_COLUMN_8 = "SESSION_DATE";
    public static final String CSV_HEADER_COLUMN_9 = "ACTIVITY";
    public static final String CSV_HEADER_COLUMN_10 = "ACTIVITY_TYPE";
    public static final String CSV_HEADER_COLUMN_11 = "ACTIVITY_TIMESTAMP";
}
