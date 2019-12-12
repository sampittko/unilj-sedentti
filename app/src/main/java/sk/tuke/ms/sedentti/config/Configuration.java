package sk.tuke.ms.sedentti.config;

import android.os.Environment;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;

import java.io.File;

/**
 * List of contants suited for changes by the developer
 */
public abstract class Configuration {
    //
    // General app config
    //
    public static final boolean FIRST_TIME_STARTUP_PERFORMED = false;
    public static final boolean USING_ARTIFICIAL_PROFILE = false;
    public static final String APP_NAME = "Sedentti";
    public static final String APP_PACKAGE = "sk.tuke.ms.sedentti";

    //
    // Firebase Authentication
    //
    public static final String PROFILE_UNKNOWN_DISPLAY_NAME = "Unknown Name";
    public static final String PROFILE_UNKNOWN_EMAIL = "";
    public static final String PROFILE_UNKNOWN_PHOTO_URL = "";
    public static final String PROFILE_ARTIFICIAL_NAME = "Robot 4019";
    public static final String PROFILE_ARTIFICIAL_EMAIL = "robot@4019.hack";
    public static final String PROFILE_ARTIFICIAL_PHOTO_URL = PROFILE_UNKNOWN_PHOTO_URL;
    public static final String PROFILE_ARTIFICIAL_FIREBASE_AUTH_ID = "eD74z7vXByOTxV1tVAq87g0mVhx1"; // id corresponding to the real one from FA

    //
    // Firebase Cloud Storage
    //
    public static final String CLOUD_STORAGE_DATE_PATH_SEPARATOR = "-";
    public static final String CLOUD_STORAGE_FILENAME_PREFIX = "export";

    //
    // Local Database
    //
    public static final String LOCAL_DATABASE_NAME = APP_PACKAGE + ".local.db";
    public static final int LOCAL_DATABASE_VERSION = 5;

    //
    // Upload work
    //
    public static final NetworkType UPLOAD_WORK_NETWORK_TYPE = NetworkType.CONNECTED;
    public static final String UPLOAD_WORK_NAME = "UPLOAD_WORK";
    public static final ExistingPeriodicWorkPolicy UPLOAD_WORK_POLICY = ExistingPeriodicWorkPolicy.KEEP;
    public static final int UPLOAD_WORK_RESULT_WAITING_THREAD_SLEEP_MILLISECONDS_LENGTH = 1000;
    public static final int UPLOAD_WORK_WAITING_MINUTES = 10;

    //
    // DatabaseExporter
    //
    public static final String DB_EXPORTER_FILE_TYPE = ".csv";
    public static final String DB_EXPORTER_FILENAME = APP_NAME + "-Export" + DB_EXPORTER_FILE_TYPE;
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_1 = "USER";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_2 = "SESSION";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_3 = "SESSION_SEDENTARY";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_4 = "SESSION_START_TIMESTAMP";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_5 = "SESSION_END_TIMESTAMP";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_6 = "SESSION_DURATION";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_7 = "SESSION_SUCCESSFUL";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_8 = "SESSION_DATE";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_9 = "ACTIVITY";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_10 = "ACTIVITY_TYPE";
    public static final String DB_EXPORTER_CSV_HEADER_COLUMN_11 = "ACTIVITY_TIMESTAMP";
    public static final File DB_EXPORTER_EXPORT_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // TODO set database file export location to the different one (currently Downloads folder)
}
