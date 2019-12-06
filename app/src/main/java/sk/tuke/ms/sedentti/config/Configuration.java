package sk.tuke.ms.sedentti.config;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;

/**
 * List of contants suited for changes by the developer
 */
public abstract class Configuration {
    public static final boolean FIRST_TIME_STARTUP_PERFORMED = false;

    //
    // Firebase Cloud Storage
    //
    public static final String STORAGE_DATE_PATH_SEPARATOR = "-";
    public static final String STORAGE_FILE_TYPE = ".csv";
    public static final int STORAGE_MINUTES_UPLOAD_INTERVAL = 10;

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
}
