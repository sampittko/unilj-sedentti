package sk.tuke.ms.sedentti.firebase.uploader;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.firebase.helper.StorageHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.UploadTask;
import sk.tuke.ms.sedentti.model.exporter.DatabaseExporter;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.model.helper.UploadTaskHelper;

public class UploadWorker extends Worker {
    private static final String TAG = "UploadWorker";

    private UploadTaskHelper uploadTaskHelper;
    private SessionHelper sessionHelper;

    private Profile profile;

    private Result firebaseUploadTaskResult;

    private boolean sessionUriSaved;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        ProfileHelper profileHelper = new ProfileHelper(context);

        try {
            profile = profileHelper.getActive();
            this.uploadTaskHelper = new UploadTaskHelper(context, profileHelper.getActive());
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
            latestUploadTask = uploadTaskHelper.getLatest();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (latestUploadTask != null && !latestUploadTask.isProcessed()) {
            Crashlytics.log(Log.DEBUG, TAG, "Continuing unprocessed upload task");

            String dbFilePath;
            try {
                dbFilePath = DatabaseExporter.getExistingFilePath();
            } catch (FileNotFoundException e) {
                Crashlytics.log(Log.DEBUG, TAG, "File for upload not found");
                try {
                    Crashlytics.log(Log.DEBUG, TAG, "Regenerating file");
                    dbFilePath = new DatabaseExporter(getApplicationContext(), profile).generateFile(true);
                } catch (SQLException ex) {
                    Crashlytics.log(Log.DEBUG, TAG, "Work failure and will be retried later on");
                    ex.printStackTrace();
                    return Result.retry();
                }
            }
            if (latestUploadTask.getSessionUriString() != null) {
                Crashlytics.log(Log.DEBUG, TAG, "Upload task session URI exists, retrying upload");
                Uri sessionUri = Uri.parse(latestUploadTask.getSessionUriString());
                return performUploadForResult(dbFilePath, sessionUri);
            }
            else {
                Crashlytics.log(Log.DEBUG, TAG, "Upload task session URI does not exist, undoing the whole previous work");
                return undoPreviousWorkForResult(latestUploadTask);
            }
        }
        else {
            Crashlytics.log(Log.DEBUG, TAG, "No unprocessed upload task found");
            if (newDataAvailable()) {
                Crashlytics.log(Log.DEBUG, TAG, "New data for upload available");

                String dbFilePath;
                try {
                    dbFilePath = new DatabaseExporter(getApplicationContext(), profile).generateFile(false);
                } catch (SQLException e) {
                    Crashlytics.log(Log.DEBUG, TAG, "Work failure and will be retried later on");
                    e.printStackTrace();
                    return Result.retry();
                }
                return performUploadForResult(dbFilePath, null);
            }
            else {
                Crashlytics.log(Log.DEBUG, TAG, "New data for upload not available");
                return Result.success();
            }
        }
    }

    @NotNull
    private Result undoPreviousWorkForResult(UploadTask latestUploadTask) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing undoWork");
        try {
            sessionHelper.revertExported();
            Crashlytics.log(Log.DEBUG, TAG, "Sessions reverted");
            uploadTaskHelper.cancel(latestUploadTask);
            Crashlytics.log(Log.DEBUG, TAG, "Upload task canceled");
            return Result.success();
        } catch (SQLException e) {
            Crashlytics.log(Log.DEBUG, TAG, "Unsuccessful revert process, retrying later on");
            e.printStackTrace();
            return Result.retry();
        }
    }

    private boolean newDataAvailable() {
        try {
            return sessionHelper.getNotExportedFinishedCount() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Result performUploadForResult(String dbFilePath, Uri sessionUri) {
        Crashlytics.log(Log.DEBUG, TAG, "Starting to perform the upload");

        UploadTask uploadTask;
        StorageReference storageRef;
        File dbFile = new File(dbFilePath);

        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            uploadTask = uploadTaskHelper.startNew(dbFile);
            storageRef = storage.getReference(
                    new StorageHelper(getApplicationContext(), profile).getPath()
            );
            Crashlytics.log(Log.DEBUG, TAG, "Created storage reference with path equal to " + storageRef.getPath());
            Uri dbFileUri = Uri.fromFile(dbFile);
            com.google.firebase.storage.UploadTask firebaseUploadTask = storageRef.putFile(dbFileUri, new StorageMetadata.Builder().build(), sessionUri);
            sessionUriSaved = false;
            setUploadTaskProgressListeners(uploadTask, firebaseUploadTask);
            Crashlytics.log(Log.DEBUG, TAG, "Upload has just started ");
        } catch (SQLException e) {
            e.printStackTrace();
            firebaseUploadTaskResult = Result.retry();
        }
        return waitForResult();
    }

    private Result waitForResult() {
        while (firebaseUploadTaskResult == null) {
            try {
                Thread.sleep(Configuration.UPLOAD_WORK_RESULT_WAITING_THREAD_SLEEP_MILLISECONDS_LENGTH);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return firebaseUploadTaskResult;
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
                sessionHelper.setExportedToUploaded();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            firebaseUploadTaskResult = Result.success();
        }).addOnProgressListener(taskSnapshot -> {
            Crashlytics.log(Log.DEBUG, TAG, "Upload is in progress (" + taskSnapshot.getBytesTransferred() + "/" + taskSnapshot.getTotalByteCount() + ")");

            Uri sessionUri = taskSnapshot.getUploadSessionUri();
            if (sessionUri != null && !sessionUriSaved) {
                sessionUriSaved = true;
                uploadTask.setSessionUriString(taskSnapshot.getUploadSessionUri().toString());
            }

            try {
                uploadTask.setBytesTransferred(taskSnapshot.getBytesTransferred());
                uploadTaskHelper.updateProgress(uploadTask);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
