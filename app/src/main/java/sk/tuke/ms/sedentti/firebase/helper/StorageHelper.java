package sk.tuke.ms.sedentti.firebase.helper;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.helper.DateHelper;
import sk.tuke.ms.sedentti.model.helper.UploadTaskHelper;

public class StorageHelper {
    private UploadTaskHelper uploadTaskHelper;

    private Profile profile;

    public StorageHelper(Context context, Profile profile) {
        this.uploadTaskHelper = new UploadTaskHelper(context, profile);
        this.profile = profile;
    }

    @NotNull
    public String getPath() throws SQLException {
        return PredefinedValues.CLOUD_STORAGE_FOLDER_SEPARATOR +
                Configuration.EVALUATION_SESSION_NUMBER +
                PredefinedValues.CLOUD_STORAGE_FOLDER_SEPARATOR +
                profile.getFirebaseAuthUid() +
                PredefinedValues.CLOUD_STORAGE_FOLDER_SEPARATOR +
                getPathDate() +
                PredefinedValues.CLOUD_STORAGE_FOLDER_SEPARATOR +
                Configuration.CLOUD_STORAGE_FILENAME_PREFIX + "-" + (uploadTaskHelper.getTodaysCorrectlyProcessedCount() + 1) +
                Configuration.DB_EXPORTER_FILE_TYPE;
    }

    @NotNull
    private static String getPathDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        return DateHelper.getDay(calendar) +
                Configuration.CLOUD_STORAGE_DATE_PATH_SEPARATOR +
                (DateHelper.getMonth(calendar) + 1) +
                Configuration.CLOUD_STORAGE_DATE_PATH_SEPARATOR +
                DateHelper.getYear(calendar);
    }
}
