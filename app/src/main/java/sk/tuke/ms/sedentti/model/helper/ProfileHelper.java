package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.helper.SharedPreferencesHelper;
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

    /**
     * @return Currently active profile based on shared preferences
     * @throws SQLException In case that communication with DB was not successful
     */
    public Profile getActiveProfile() throws SQLException {
        return profileDao.queryForId(
                new SharedPreferencesHelper(context).getActiveProfileId()
        );
    }

    /**
     * @param name String containing the name of future profile
     * @return New profile object
     * @throws SQLException In case that communication with DB was not successful
     */
    public Profile createNewProfile(String name) throws SQLException {
        Profile profile = new Profile(name);
        profileDao.create(profile);
        return profile;
    }

    /**
     * @return Some existing profile
     * @throws SQLException In case that communication with DB was not successful
     */
    public Profile getExistingProfile() throws SQLException {
        return profileDao.queryForAll().get(0);
    }

    /**
     * @return Number of existing profiles in database
     * @throws SQLException In case that communication with DB was not successful
     */
    public int getNumberOfExistingProfiles() throws SQLException {
        return (int) profileDao.countOf();
    }
}
