package sk.tuke.ms.sedentti.firebase.helper;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.config.PredefinedValues;

public class StorageHelper {
    @NotNull
    public static String getPath(String activeProfileFirebaseAuthUid, int todaysUploadTasksCount) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder
                .append(PredefinedValues.STORAGE_FOLDER_SEPARATOR)
                .append(activeProfileFirebaseAuthUid)
                .append(PredefinedValues.STORAGE_FOLDER_SEPARATOR)
                .append(StorageHelper.getPathDate())
                .append(PredefinedValues.STORAGE_FOLDER_SEPARATOR)
                .append(todaysUploadTasksCount + 1)
                .append(Configuration.STORAGE_FILE_TYPE)
                .toString();
    }

    @NotNull
    private static String getPathDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder
                .append(getDay(calendar))
                .append(Configuration.STORAGE_DATE_PATH_SEPARATOR)
                .append(getMonth(calendar))
                .append(Configuration.STORAGE_DATE_PATH_SEPARATOR)
                .append(getYear(calendar))
                .toString();
    }

    private static int getDay(@NotNull Calendar calendar) {
        return calendar.get(Calendar.DATE);
    }

    private static int getMonth(@NotNull Calendar calendar) {
        return calendar.get(Calendar.MONTH);
    }

    private static int getYear(@NotNull Calendar calendar) {
        return calendar.get(Calendar.YEAR);
    }
}
