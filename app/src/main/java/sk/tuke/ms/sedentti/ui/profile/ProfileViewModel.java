package sk.tuke.ms.sedentti.ui.profile;

import android.app.Application;
import android.os.AsyncTask;

import java.sql.SQLException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;

public class ProfileViewModel extends AndroidViewModel {


    private ProfileHelper profileHelper;

    private MutableLiveData<Profile> profile;

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        this.profileHelper = new ProfileHelper(this.getApplication());
    }

    public MutableLiveData<Profile> getProfile() {
        if (this.profile == null) {
            this.profile = new MutableLiveData<Profile>();
            loadProfile();
        }
        return profile;
    }

    private void loadProfile() {
        new loadProfileAsyncTask().execute();
    }

    private class loadProfileAsyncTask extends AsyncTask<Void, Void, Profile> {
        @Override
        protected Profile doInBackground(Void... voids) {
            try {
                return profileHelper.getActive();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile result) {
            profile.postValue(result);
        }
    }
}
