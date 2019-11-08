package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.helper.CommonValues;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class ProfileHelper {
    private Dao<Profile, Long> profileDao;

    private Context context;

    public ProfileHelper(Context context) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            profileDao = databaseHelper.profileDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.context = context;
    }

    public Profile getActiveProfile() throws SQLException {
        return profileDao.queryForId(getActiveProfileId());
    }

    private long getActiveProfileId() {
        SharedPreferences profileShPr = context.getSharedPreferences(CommonValues.PROFILE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return profileShPr.getLong(CommonValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID, CommonValues.PROFILE_SHARED_PREFERENCES_ACTIVE_ID_DEFAULT);
    }
}
