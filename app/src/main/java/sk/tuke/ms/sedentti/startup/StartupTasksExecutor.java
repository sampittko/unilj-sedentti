package sk.tuke.ms.sedentti.startup;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.recognition.activity.ActivityRecognitionService;
import sk.tuke.ms.sedentti.work.upload.UploadWorkManager;

public class StartupTasksExecutor {
    private static final String TAG = "StartupTasksExecutor";

    private Context context;
    private ProfileHelper profileHelper;
    private Profile activeProfile;
    private UploadWorkManager uploadWorkerManager;

    public StartupTasksExecutor(Context context) throws SQLException {
        this.context = context;
        this.profileHelper = new ProfileHelper(context);
        this.activeProfile = profileHelper.getActive();
        uploadWorkerManager = new UploadWorkManager(context);
    }

    public void execute(boolean crashlyticsSetup) {
        if (crashlyticsSetup) {
            profileHelper.setCrashlyticsUser(activeProfile);
            Crashlytics.log(Log.DEBUG, TAG, "Crashlytics user details set");
        }
        startForegroundService();
        uploadWorkerManager.activateUploadWorker();
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
}
