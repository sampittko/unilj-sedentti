package sk.tuke.ms.sedentti.work.upload;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.firebase.UploadScheduler;
import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;

public class UploadWorkManager {
    private static final String TAG = "UploadWorkManager";

    private AppSPHelper appSPHelper;
    private Profile activeProfile;
    private Context context;
    private WorkManager workManager;

    public UploadWorkManager(Context context) throws SQLException {
        this.context = context;
        this.appSPHelper = new AppSPHelper(context);
        this.activeProfile = new ProfileHelper(context).getActive();
        this.workManager = WorkManager.getInstance(context);
    }

    public void activateUploadWorker() {
        workManager
                .enqueueUniquePeriodicWork(
                        Configuration.UPLOAD_WORK_NAME,
                        Configuration.UPLOAD_WORK_POLICY,
                        getUploadWorkerRequest()
                );
        Crashlytics.log(Log.DEBUG, TAG, "Upload work scheduled");
    }

    public void restartUploadWork() {
        workManager.cancelUniqueWork(Configuration.UPLOAD_WORK_NAME).getResult().addListener(this::activateUploadWorker, runnable -> {});
    }

    @NotNull
    private PeriodicWorkRequest getUploadWorkerRequest() {
        Crashlytics.log(Log.DEBUG, TAG, "Building upload work request");
        return new PeriodicWorkRequest.Builder(UploadWorker.class, appSPHelper.getSyncInterval(), TimeUnit.MILLISECONDS)
                .setConstraints(getUploadWorkerConstraints())
                .setInitialDelay(getUploadWorkerInitialMillisDelay(), TimeUnit.MILLISECONDS)
                .build();
    }

    private long getUploadWorkerInitialMillisDelay() {
        try {
            long initialMillisDelay = new UploadScheduler(context, activeProfile).getInitialMillisecondsDelay();
            Crashlytics.log(Log.DEBUG, TAG, "Initial milliseconds delay for upload work is " + initialMillisDelay);
            return initialMillisDelay;
        } catch (SQLException e) {
            e.printStackTrace();
            Crashlytics.log(Log.DEBUG, TAG, "Unable to get initial milliseconds delay for upload work, setting UPLOAD_WORK_WAITING_MILLIS");
            return appSPHelper.getSyncInterval();
        }
    }

    @NotNull
    private Constraints getUploadWorkerConstraints() {
        Crashlytics.log(Log.DEBUG, TAG, "Getting upload work constraints");
        return new Constraints.Builder()
                .setRequiredNetworkType(Configuration.UPLOAD_WORK_NETWORK_TYPE)
                .build();
    }
}
