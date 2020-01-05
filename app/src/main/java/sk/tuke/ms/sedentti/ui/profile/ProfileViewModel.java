package sk.tuke.ms.sedentti.ui.profile;

import android.app.Application;
import android.os.AsyncTask;
import android.widget.Toast;

import java.sql.SQLException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class ProfileViewModel extends AndroidViewModel {


    private Profile activeProfile;
    private ProfileHelper profileHelper;
    private SessionHelper sessionHelper;

    private MutableLiveData<Integer> success;
    private MutableLiveData<Integer> streak;
    private MutableLiveData<Integer> finishedCount;

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        ProfileHelper profileHelper = new ProfileHelper(this.getApplication());

        try {
            this.activeProfile = profileHelper.getActive();
        } catch (SQLException e) {
            Toast.makeText(this.getApplication(), "Error, no profile", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        this.sessionHelper = new SessionHelper(this.getApplication(), activeProfile);
    }

    public Profile getProfile() {
        return this.activeProfile;
    }

    public LiveData<Integer> getStreak() {
        if (this.streak == null) {
            this.streak = new MutableLiveData<Integer>();
            loadStreak();
        }
        return this.streak;
    }

    private void loadStreak() {
        new loadStreakAsyncTask().execute();
    }

    public LiveData<Integer> getSuccess() {
        if (this.success == null) {
            this.success = new MutableLiveData<Integer>();
            loadSuccess();
        }
        return this.success;
    }

    private void loadSuccess() {
        new loadSuccessAsyncTask().execute();
    }

    public MutableLiveData<Integer> getFinishedCount() {
        if (this.finishedCount == null) {
            this.finishedCount = new MutableLiveData<Integer>();
            loadFinishedCount();
        }
        return this.finishedCount;
    }

    private void loadFinishedCount() {
        new loadFinishedCountAsyncTask().execute();
    }

    private class loadStreakAsyncTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                return sessionHelper.getStreak();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            streak.postValue(result);
        }
    }

    private class loadSuccessAsyncTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                return sessionHelper.getSuccessRate(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            success.postValue(result);
        }
    }

    private class loadFinishedCountAsyncTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                return sessionHelper.getFinishedCount();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            finishedCount.postValue(result);
        }
    }


//    private void loadProfile() {
//        new loadProfileAsyncTask().execute();
//    }
//
//    private class loadProfileAsyncTask extends AsyncTask<Void, Void, Profile> {
//        @Override
//        protected Profile doInBackground(Void... voids) {
//            try {
//                return profileHelper.getActive();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            } catch (NullPointerException e) {
//                return null;
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Profile result) {
//            profile.postValue(result);
//        }
//    }
}
