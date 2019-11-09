package sk.tuke.ms.sedentti.ui.home;

import android.app.Application;
import android.os.AsyncTask;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class HomeViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<Session>> sessions;
    private MutableLiveData<Integer> success;
    private MutableLiveData<Integer> streak;
    private MutableLiveData<Long> activeSessionDuration;
    private SessionHelper sessionHelper;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        ProfileHelper profileHelper = new ProfileHelper(this.getApplication());

        Profile activeProfile = null;
        try {
            activeProfile = profileHelper.getActiveProfile();
        } catch (SQLException e) {
            Toast.makeText(this.getApplication(), "Error, no profile", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        this.sessionHelper = new SessionHelper(this.getApplication(), activeProfile);
    }

    public MutableLiveData<Integer> getStreak() {
        if (this.streak == null) {
            this.streak = new MutableLiveData<Integer>();
            loadStreak();
        }
        return this.streak;
    }

    private void loadStreak() {
        new loadStreakAsyncTask(this.sessionHelper).execute();
    }

    public MutableLiveData<Integer> getSuccess() {
        if (this.success == null) {
            this.success = new MutableLiveData<Integer>();
            loadSuccess();
        }
        return this.success;
    }

    private void loadSuccess() {
        new loadSuccessAsyncTask(this.sessionHelper).execute();
    }

    public MutableLiveData<Long> getActiveSessionDuration() {
        if (this.activeSessionDuration == null) {
            this.activeSessionDuration = new MutableLiveData<Long>();
            loadActiveSessionDuration();
        }
        return this.activeSessionDuration;
    }

    private void loadActiveSessionDuration() {
        new loadActiveSessionDurationAsyncTask(this.sessionHelper).execute();
    }

    public LiveData<ArrayList<Session>> getHomeTimelineSessions() {
        if (this.sessions == null) {
            this.sessions = new MutableLiveData<ArrayList<Session>>();
            loadSessions();
        }
        return this.sessions;
    }

    private void loadSessions() {
        new loadSessionsAsyncTask(this.sessionHelper).execute();
    }

    private class loadSessionsAsyncTask extends AsyncTask<Void, Void, ArrayList<Session>> {

        private SessionHelper sessionHelper;

        loadSessionsAsyncTask(SessionHelper sessionHelper) {
            this.sessionHelper = sessionHelper;
        }

        @Override
        protected ArrayList<Session> doInBackground(Void... voids) {
            try {
                return sessionHelper.getHomeTimelineSessions();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Session> result) {
            sessions.postValue(result);
        }
    }

    private class loadStreakAsyncTask extends AsyncTask<Void, Void, Integer> {
        private SessionHelper sessionHelper;

        loadStreakAsyncTask(SessionHelper sessionHelper) {
            this.sessionHelper = sessionHelper;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return this.sessionHelper.getStreak();
        }

        @Override
        protected void onPostExecute(Integer result) {
            streak.postValue(result);
        }
    }

    private class loadSuccessAsyncTask extends AsyncTask<Void, Void, Integer> {
        private SessionHelper sessionHelper;

        loadSuccessAsyncTask(SessionHelper sessionHelper) {
            this.sessionHelper = sessionHelper;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                return this.sessionHelper.getSuccessRate();
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

    private class loadActiveSessionDurationAsyncTask extends AsyncTask<Void, Void, Long> {
        private SessionHelper sessionHelper;

        loadActiveSessionDurationAsyncTask(SessionHelper sessionHelper) {
            this.sessionHelper = sessionHelper;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            try {
                return this.sessionHelper.getPendingSessionDuration();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {
            activeSessionDuration.postValue(result);
        }
    }
}