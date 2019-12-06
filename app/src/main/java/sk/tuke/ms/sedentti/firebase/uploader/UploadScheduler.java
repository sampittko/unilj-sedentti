package sk.tuke.ms.sedentti.firebase.uploader;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Calendar;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.UploadTask;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.model.helper.UploadTaskHelper;

public class UploadScheduler {
    private static final String TAG = "UploadScheduler";

    public static long getInitialMillisecondsDelay(@NotNull Context context, @NotNull Profile profile) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getInitialMillisecondsDelay");
        Crashlytics.log(Log.DEBUG, TAG, "@context: " + context.getPackageCodePath());
        Crashlytics.log(Log.DEBUG, TAG, "@profile: " + profile.getFirebaseAuthUid());

        UploadTask latestUploadTask = getLatestUploadTask(context, profile);

        if (latestUploadTask == null) {
            Crashlytics.log(Log.DEBUG, TAG, "There is no previous upload task");
            if (getSessionsCount(context, profile) > 0) {
                Crashlytics.log(Log.DEBUG, TAG, "Perform upload work now");
                return 0L;
            }
            else {
                Crashlytics.log(Log.DEBUG, TAG, "Perform upload work in STORAGE_MINUTES_UPLOAD_INTERVAL");
                return Configuration.STORAGE_MINUTES_UPLOAD_INTERVAL;
            }
        }

        if (!latestUploadTask.isSuccessful()) {
            Crashlytics.log(Log.DEBUG, TAG, "Last upload task was not successful so it will retry immediately");
            return 0L;
        }

        return getTimeDiff(latestUploadTask);
    }

    private static int getSessionsCount(Context context, Profile profile) {
        SessionHelper sessionHelper = new SessionHelper(context, profile);
        try {
            return sessionHelper.getFinishedSessionsCount();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static UploadTask getLatestUploadTask(Context context, Profile profile) {
        UploadTaskHelper uploadTaskHelper = new UploadTaskHelper(context, profile);
        UploadTask latestUploadTask = null;

        try {
            latestUploadTask = uploadTaskHelper.getLatestUploadTask();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return latestUploadTask;
    }

    private static long getTimeDiff(@NotNull UploadTask latestUploadTask) {
        Crashlytics.log(Log.DEBUG, TAG, "Last upload task was successful and the next one will be scheduled for times difference");

        Calendar currentDate = Calendar.getInstance();

        Calendar dueDate = Calendar.getInstance();
        dueDate.setTimeInMillis(latestUploadTask.getStartTimestamp());

        while (dueDate.before(currentDate)) {
            dueDate.add(Calendar.MINUTE, Configuration.STORAGE_MINUTES_UPLOAD_INTERVAL);
        }

        return dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
    }
}