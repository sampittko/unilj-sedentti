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

    private SessionHelper sessionHelper;
    private UploadTaskHelper uploadTaskHelper;

    public UploadScheduler(Context context, Profile profile) {
        sessionHelper = new SessionHelper(context, profile);
        uploadTaskHelper = new UploadTaskHelper(context, profile);
    }

    public long getInitialMillisecondsDelay() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getInitialMillisecondsDelay");

        UploadTask latestUploadTask = uploadTaskHelper.getLatestUploadTask();

        if (latestUploadTask == null) {
            Crashlytics.log(Log.DEBUG, TAG, "There is no previous upload task");
            if (sessionHelper.getFinishedSessionsCount() > 0) {
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

    private long getTimeDiff(@NotNull UploadTask latestUploadTask) {
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
