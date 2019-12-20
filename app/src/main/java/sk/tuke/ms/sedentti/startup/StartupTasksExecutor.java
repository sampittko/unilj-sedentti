package sk.tuke.ms.sedentti.startup;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.firebase.uploader.UploadScheduler;
import sk.tuke.ms.sedentti.firebase.uploader.UploadWorker;
import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.recognition.activity.ActivityRecognitionService;

public class StartupTasksExecutor {
    private static final String TAG = "StartupTasksExecutor";

    private Context context;
    private ProfileHelper profileHelper;
    private Profile activeProfile;

    public StartupTasksExecutor(Context context) throws SQLException {
        this.context = context;
        this.profileHelper = new ProfileHelper(context);
        this.activeProfile = profileHelper.getActive();
    }

    public void execute(boolean crashlyticsSetup) {
        if (crashlyticsSetup) {
            profileHelper.setCrashlyticsUser(activeProfile);
            Crashlytics.log(Log.DEBUG, TAG, "Crashlytics user details set");
        }
        setAppDefaultSettings();
        startForegroundService();
        activateUploadWorker();
    }

    private void setAppDefaultSettings() {
        AppSPHelper appSPHelper = new AppSPHelper(context);
        appSPHelper.setAppDefaultSettings();
        Crashlytics.log(Log.DEBUG, TAG, "App settings were set to defaults");
    }

    private void startForegroundService() {
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        intent.setAction(PredefinedValues.COMMAND_INIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        Crashlytics.log(Log.DEBUG, TAG, "Activity recognition foreground service started");
    }

    private void activateUploadWorker() {
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        Configuration.UPLOAD_WORK_NAME,
                        Configuration.UPLOAD_WORK_POLICY,
                        getUploadWorkerRequest()
                );
        Crashlytics.log(Log.DEBUG, TAG, "Upload work scheduled");
    }

    @NotNull
    private PeriodicWorkRequest getUploadWorkerRequest() {
        Crashlytics.log(Log.DEBUG, TAG, "Building upload work request");
        return new PeriodicWorkRequest.Builder(UploadWorker.class, Configuration.UPLOAD_WORK_WAITING_MILLIS, TimeUnit.MILLISECONDS)
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
            return Configuration.UPLOAD_WORK_WAITING_MILLIS;
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
