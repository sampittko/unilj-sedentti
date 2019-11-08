package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.helper.SharedPreferencesHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class    ProfileHelper {
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
        return profileDao.queryForId(
                new SharedPreferencesHelper(context).getActiveProfileId()
        );
    }

    public Profile createNewProfile(String name) throws SQLException {
        Profile profile = new Profile(name);
        profileDao.create(profile);
        return profile;
    }

    public Profile getExistingProfile() throws SQLException {
        return profileDao.queryForAll().get(0);
    }

    public int getNumberOfExistingProfiles() throws SQLException {
        return (int) profileDao.countOf();
    }
}
