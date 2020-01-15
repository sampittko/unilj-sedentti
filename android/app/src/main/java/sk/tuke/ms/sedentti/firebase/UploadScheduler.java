package sk.tuke.ms.sedentti.firebase;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Calendar;

import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.UploadTask;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.model.helper.UploadTaskHelper;

public class UploadScheduler {
    private static final String TAG = "UploadScheduler";

    private SessionHelper sessionHelper;
    private UploadTaskHelper uploadTaskHelper;
    private AppSPHelper appSPHelper;

    public UploadScheduler(Context context, Profile profile) {
        sessionHelper = new SessionHelper(context, profile);
        uploadTaskHelper = new UploadTaskHelper(context, profile);
        appSPHelper = new AppSPHelper(context);
    }

    public long getInitialMillisecondsDelay() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getInitialMillisecondsDelay");

        UploadTask latestUploadTask = uploadTaskHelper.getLatest();

        if (latestUploadTask == null) {
            Crashlytics.log(Log.DEBUG, TAG, "There is no previous upload task");
            if (sessionHelper.getFinishedCount() > 0) {
                Crashlytics.log(Log.DEBUG, TAG, "Perform upload work now");
                return 0L;
            }
            else {
                Crashlytics.log(Log.DEBUG, TAG, "Perform upload work in UPLOAD_WORK_WAITING_MILLIS");
                return appSPHelper.getSyncInterval();
            }
        }

        if (!UploadTaskHelper.wasSuccessful(latestUploadTask)) {
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

        int syncInterval = appSPHelper.getSyncInterval();

        while (dueDate.before(currentDate)) {
            dueDate.add(Calendar.MILLISECOND, syncInterval);
        }

        return dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
    }
}
