package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;

import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.UploadTask;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class UploadTaskHelper {
    private final String TAG = "UploadTaskHelper";

    private Dao<UploadTask, Long> uploadTaskDao;
    private QueryBuilder<UploadTask, Long> uploadTaskDaoQueryBuilder;

    private Profile profile;

    public UploadTaskHelper(Context context, Profile profile) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

        try {
            this.uploadTaskDao = databaseHelper.uploadTaskDao();
            this.uploadTaskDaoQueryBuilder = uploadTaskDao.queryBuilder();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.profile = profile;
    }

    public UploadTask getLatestUploadTask() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getLatestUploadTask");

        return uploadTaskDaoQueryBuilder
                .orderBy(UploadTask.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(UploadTask.COLUMN_PROFILE_ID, profile.getId())
                .queryForFirst();
    }

    public int getTodaysUploadTasksCount() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getTodaysUploadTasksCount");

        return (int) uploadTaskDaoQueryBuilder
                .where()
                .eq(UploadTask.COLUMN_DATE, DateHelper.getNormalizedDate(new Date()))
                .and()
                .ne(UploadTask.COLUMN_END_TIMESTAMP, 0L)
                .and()
                .eq(UploadTask.COLUMN_PROFILE_ID, profile.getId())
                .countOf();
    }

    public UploadTask startNewUploadTask(@NotNull File dbFile) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing startNewUploadTask");
        Crashlytics.log(Log.DEBUG, TAG, "@dbFile: " + dbFile.getPath());

        UploadTask uploadTask = new UploadTask(
                new Date().getTime(),
                dbFile.length(),
                dbFile.getPath(),
                profile
        );

        uploadTaskDao.create(uploadTask);

        return uploadTask;
    }

    public void failure(@NotNull UploadTask uploadTask, @NotNull Exception exception) throws SQLException {
        uploadTask.setSuccessful(false);
        uploadTask.setError(Objects.requireNonNull(exception.getMessage()));
        end(uploadTask);
    }

    public void success(@NotNull UploadTask uploadTask) throws SQLException {
        uploadTask.setSuccessful(true);
        uploadTask.setBytesTransferred(uploadTask.getBytesTotal());
        end(uploadTask);
    }

    private void end(@NotNull UploadTask uploadTask) throws SQLException {
        long endTimestamp = new Date().getTime();
        uploadTask.setEndTimestamp(endTimestamp);
        uploadTask.setDuration(endTimestamp - uploadTask.getStartTimestamp());
        uploadTaskDao.update(uploadTask);
    }

    public void updateProgress(@NotNull UploadTask uploadTask, long bytesTransferred) throws SQLException {
        uploadTask.setBytesTransferred(bytesTransferred);
        uploadTask.setDuration(new Date().getTime() - uploadTask.getStartTimestamp());
        uploadTaskDao.update(uploadTask);
    }
}
