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

    /**
     * @return
     * @throws SQLException
     */
    public UploadTask getLatest() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getLatest");

        uploadTaskDaoQueryBuilder.reset();

        return uploadTaskDaoQueryBuilder
                .orderBy(UploadTask.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(UploadTask.COLUMN_PROFILE_ID, profile.getId())
                .queryForFirst();
    }

    /**
     * @return
     * @throws SQLException
     */
    public int getTodaysCount() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getTodaysCount");

        uploadTaskDaoQueryBuilder.reset();

        return (int) uploadTaskDaoQueryBuilder
                .where()
                .eq(UploadTask.COLUMN_DATE, DateHelper.getNormalized(new Date()))
                .and()
                .ne(UploadTask.COLUMN_END_TIMESTAMP, 0L)
                .and()
                .eq(UploadTask.COLUMN_PROFILE_ID, profile.getId())
                .countOf();
    }

    /**
     * @param dbFile
     * @return
     * @throws SQLException
     */
    public UploadTask startNew(@NotNull File dbFile) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing startNew");
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

    /**
     * @param uploadTask
     * @param exception
     * @throws SQLException
     */
    public void failure(@NotNull UploadTask uploadTask, @NotNull Exception exception) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing failure");
        Crashlytics.log(Log.DEBUG, TAG, "@uploadTask: " + uploadTask.getId());
        Crashlytics.log(Log.DEBUG, TAG, "@exception: " + exception.getMessage());

        uploadTask.setSuccessful(false);
        uploadTask.setError(exception.getMessage());
        end(uploadTask);
    }

    /**
     * @param uploadTask
     * @throws SQLException
     */
    public void success(@NotNull UploadTask uploadTask) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing success");
        Crashlytics.log(Log.DEBUG, TAG, "@uploadTask: " + uploadTask.getId());

        uploadTask.setSuccessful(true);
        uploadTask.setBytesTransferred(uploadTask.getBytesTotal());
        end(uploadTask);
    }

    private void end(@NotNull UploadTask uploadTask) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing end");
        Crashlytics.log(Log.DEBUG, TAG, "@uploadTask: " + uploadTask.getId());

        long endTimestamp = new Date().getTime();
        uploadTask.setEndTimestamp(endTimestamp);
        uploadTask.setDuration(endTimestamp - uploadTask.getStartTimestamp());
        uploadTaskDao.update(uploadTask);
    }

    /**
     * @param uploadTask
     * @param bytesTransferred
     * @throws SQLException
     */
    public void updateProgress(@NotNull UploadTask uploadTask, long bytesTransferred) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing updateProgress");
        Crashlytics.log(Log.DEBUG, TAG, "@uploadTask: " + uploadTask.getId());
        Crashlytics.log(Log.DEBUG, TAG, "@bytesTransferred: " + bytesTransferred);

        uploadTask.setBytesTransferred(bytesTransferred);
        uploadTask.setDuration(new Date().getTime() - uploadTask.getStartTimestamp());
        uploadTaskDao.update(uploadTask);
    }
}
