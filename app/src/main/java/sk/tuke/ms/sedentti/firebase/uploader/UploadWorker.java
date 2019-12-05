package sk.tuke.ms.sedentti.firebase.uploader;

import android.content.Context;
import android.util.Log;

import java.sql.SQLException;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import sk.tuke.ms.sedentti.firebase.helper.StorageHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.UploadTask;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.UploadTaskHelper;

public class UploadWorker extends Worker {
    private static final String TAG = "UploadWorker";

    private UploadTaskHelper uploadTaskHelper;
    private ProfileHelper profileHelper;

    private Profile profile;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.profileHelper = new ProfileHelper(context);
        try {
            profile = profileHelper.getActiveProfile();
            this.uploadTaskHelper = new UploadTaskHelper(context, profileHelper.getActiveProfile());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        UploadTask latestUploadTask = null;

        try {
            latestUploadTask = uploadTaskHelper.getLatestUploadTask();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (latestUploadTask != null && !latestUploadTask.isSuccessful()) {
            Log.d(TAG, "Continuing unfinished upload task");
            continueWork();
        } else {
            Log.d(TAG, "No unfinished upload task found");
            if (dataAvailable()) {
                Log.d(TAG, "Data for upload available");
                generateFileDB();
                UploadTask uploadTask = getNewUploadTask();
                performUpload(uploadTask);
            }
        }

        return Result.success();
    }

    private void continueWork() {
        // TODO continueWork
    }

    private boolean dataAvailable() {
        // TODO dataAbailable
        return true;
    }

    private void generateFileDB() {
        // TODO generateFileDB
    }

    private UploadTask getNewUploadTask() {
        // TODO getNewUploadTask
        return null;
    }

    private void performUpload(UploadTask uploadTask) {
        // TODO performUpload
    }

    private String getFilePath() {
        String activeProfileFirebaseAuthUid = "";
        int todaysUploadTasksCount = 0;

        try {
            activeProfileFirebaseAuthUid = profile.getFirebaseAuthUid();
            todaysUploadTasksCount = uploadTaskHelper.getTodaysUploadTasksCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return StorageHelper.getPath(activeProfileFirebaseAuthUid, todaysUploadTasksCount);
    }
}