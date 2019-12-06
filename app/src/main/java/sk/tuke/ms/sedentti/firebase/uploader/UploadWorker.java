package sk.tuke.ms.sedentti.firebase.uploader;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import sk.tuke.ms.sedentti.firebase.helper.StorageHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.UploadTask;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;
import sk.tuke.ms.sedentti.model.exporter.DatabaseExporter;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.model.helper.UploadTaskHelper;

public class UploadWorker extends Worker {
    private static final String TAG = "UploadWorker";

    private UploadTaskHelper uploadTaskHelper;
    private ProfileHelper profileHelper;
    private DatabaseHelper databaseHelper;
    private SessionHelper sessionHelper;

    private Profile profile;

    private Result firebaseUploadTaskResult;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.databaseHelper = OpenHelperManager.getHelper(getApplicationContext(), DatabaseHelper.class);
        this.profileHelper = new ProfileHelper(context);

        try {
            profile = profileHelper.getActiveProfile();
            this.uploadTaskHelper = new UploadTaskHelper(context, profileHelper.getActiveProfile());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.sessionHelper = new SessionHelper(context, profile);
    }

    @NonNull
    @Override
    public Result doWork() {
        UploadTask latestUploadTask = null;
        firebaseUploadTaskResult = null;

        try {
            latestUploadTask = uploadTaskHelper.getLatestUploadTask();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (latestUploadTask != null && !latestUploadTask.isSuccessful()) {
            Crashlytics.log(Log.DEBUG, TAG, "Continuing unfinished upload task");
            continueWork();
            return Result.failure();
        }
        else {
            Crashlytics.log(Log.DEBUG, TAG, "No unfinished upload task found");
            if (newDataAvailable()) {
                Crashlytics.log(Log.DEBUG, TAG, "New data for upload available");

                String dbFilePath;
                try {
                    dbFilePath = new DatabaseExporter(getApplicationContext(), profile)
                            .getDatabaseAsFile(databaseHelper.getReadableDatabase());
                } catch (IOException | SQLException e) {
                    Crashlytics.log(Log.DEBUG, TAG, "Work failure and will be retried later on");
                    e.printStackTrace();
                    return Result.retry();
                }

                performUpload(dbFilePath);

                while (firebaseUploadTaskResult == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return firebaseUploadTaskResult;
            }
            else {
                Crashlytics.log(Log.DEBUG, TAG, "New data for upload not available");
                return Result.success();
            }
        }
    }

    @Contract(" -> fail")
    private void continueWork() {
        // TODO continue interrupted work
        throw new UnsupportedOperationException();
    }

    private boolean newDataAvailable() {
        try {
            return sessionHelper.getNotExportedFinishedSessionsCount() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void performUpload(String dbFilePath) {
        Crashlytics.log(Log.DEBUG, TAG, "Starting to perform the upload");

        UploadTask uploadTask;
        StorageReference storageRef;
        File dbFile = new File(dbFilePath);

        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            uploadTask = uploadTaskHelper.startNewUploadTask(dbFile);
            storageRef = storage.getReference(
                    new StorageHelper(getApplicationContext(), profile).getPath()
            );
            Crashlytics.log(Log.DEBUG, TAG, "Created storage reference with path equal to " + storageRef.getPath());
            Uri dbFileUri = Uri.fromFile(dbFile);
            com.google.firebase.storage.UploadTask firebaseUploadTask = storageRef.putFile(dbFileUri);
            setUploadTaskProgressListeners(uploadTask, firebaseUploadTask);
            Crashlytics.log(Log.DEBUG, TAG, "Upload has just started ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setUploadTaskProgressListeners(UploadTask uploadTask, @NotNull com.google.firebase.storage.UploadTask firebaseUploadTask) {
        firebaseUploadTask.addOnFailureListener(exception -> {
            Crashlytics.log(Log.DEBUG, TAG, "Upload was unsuccessful, will retry during the next work");
            try {
                uploadTaskHelper.failure(uploadTask, exception);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            firebaseUploadTaskResult = Result.retry();
        }).addOnSuccessListener(taskSnapshot -> {
            Crashlytics.log(Log.DEBUG, TAG, "Upload was successful");
            try {
                uploadTaskHelper.success(uploadTask);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            firebaseUploadTaskResult = Result.success();
        }).addOnProgressListener(taskSnapshot -> {
            Crashlytics.log(Log.DEBUG, TAG, "Upload is in progress (" + taskSnapshot.getBytesTransferred() + "/" + taskSnapshot.getTotalByteCount() + ")");
            try {
                uploadTaskHelper.updateProgress(uploadTask, taskSnapshot.getBytesTransferred());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
