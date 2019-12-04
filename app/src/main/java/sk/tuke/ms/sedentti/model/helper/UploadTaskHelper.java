package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

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

    public UploadTask getLatestUploadTask() throws SQLException {
        Log.d(TAG, "Executing getLatestUploadTask");

        return uploadTaskDaoQueryBuilder
                .orderBy(UploadTask.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(UploadTask.COLUMN_PROFILE_ID, profile.getId())
                .queryForFirst();
    }

    public int getTodaysUploadTasksCount() throws SQLException {
        Log.d(TAG, "Executing getTodaysUploadTasksCount");

        return (int) uploadTaskDaoQueryBuilder
                .where()
                .eq(UploadTask.COLUMN_DATE, DateHelper.getNormalizedDate(new Date()))
                .and()
                .eq(UploadTask.COLUMN_PROFILE_ID, profile.getId())
                .countOf();
    }
}
