package sk.tuke.ms.sedentti.ui.home;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
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
    private MutableLiveData<Session> pendingSession;

    private MutableLiveData<Long> dailySedentaryDuration;
    private MutableLiveData<Long> dailyActiveDuration;
    private SessionHelper sessionHelper;

    private Handler tickHandler;
    private Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            tickHandler.postDelayed(timeUpdater, 1000);
            loadPendingSessionDuration();
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);
        ProfileHelper profileHelper = new ProfileHelper(this.getApplication());
        this.tickHandler = new Handler();

        Profile activeProfile = null;
        try {
            activeProfile = profileHelper.getActive();
        } catch (SQLException e) {
            Toast.makeText(this.getApplication(), "Error, no profile", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        this.sessionHelper = new SessionHelper(this.getApplication(), activeProfile);

        startTicker();
    }

    private void startTicker() {
        this.tickHandler.removeCallbacks(timeUpdater);
        this.tickHandler.postDelayed(timeUpdater, 1000);
    }

    private void stopTicker() {
        this.tickHandler.removeCallbacks(timeUpdater);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopTicker();
    }

    public LiveData<Integer> getStreak() {
        if (this.streak == null) {
            this.streak = new MutableLiveData<Integer>();
            loadStreak();
        }
        return this.streak;
    }


    private void loadStreak() {
        new loadStreakAsyncTask(this.sessionHelper).execute();
    }

    public LiveData<Integer> getSuccess() {
        if (this.success == null) {
            this.success = new MutableLiveData<Integer>();
            loadSuccess();
        }
        return this.success;
    }

    private void loadSuccess() {
        new loadSuccessAsyncTask(this.sessionHelper).execute();
    }

    public LiveData<Session> getPendingSession() {
        if (this.pendingSession == null) {
            this.pendingSession = new MutableLiveData<Session>();
            loadPendingSessionDuration();
        }
        return this.pendingSession;
    }

    private void loadPendingSessionDuration() {
        new loadPendingSessionDurationAsyncTask(this.sessionHelper).execute();
    }

    public LiveData<ArrayList<Session>> getHomeTimelineSessions() {
        if (this.sessions == null) {
            this.sessions = new MutableLiveData<ArrayList<Session>>();
            loadSessions();
        }
        return this.sessions;
    }

    public LiveData<Long> getDailySedentaryDuration() {
        if (this.dailySedentaryDuration == null) {
            this.dailySedentaryDuration = new MutableLiveData<Long>();
            loadDailySedentaryTime();
        }
        return this.dailySedentaryDuration;
    }

    private void loadDailySedentaryTime() {
        new loadDailySedentaryTimeAsyncTask(this.sessionHelper).execute();
    }

    public LiveData<Long> getDailyActiveDuration() {
        if (this.dailyActiveDuration == null) {
            this.dailyActiveDuration = new MutableLiveData<Long>();
            loadDailyActiveTime();
        }
        return this.dailyActiveDuration;
    }

    private void loadDailyActiveTime() {
        new loadDailyActiveTimeAsyncTask(this.sessionHelper).execute();
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
            try {
                return this.sessionHelper.getStreak();
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

    private class loadPendingSessionDurationAsyncTask extends AsyncTask<Void, Void, Session> {
        private SessionHelper sessionHelper;

        loadPendingSessionDurationAsyncTask(SessionHelper sessionHelper) {
            this.sessionHelper = sessionHelper;
        }

        @Override
        protected Session doInBackground(Void... voids) {
            try {
                return this.sessionHelper.getPending();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Session result) {
            pendingSession.postValue(result);
        }
    }

    private class loadDailySedentaryTimeAsyncTask extends AsyncTask<Void, Void, Long> {
        private SessionHelper sessionHelper;

        public loadDailySedentaryTimeAsyncTask(SessionHelper sessionHelper) {
            this.sessionHelper = sessionHelper;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            try {
                return this.sessionHelper.getDailySedentaryDuration();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {
            dailySedentaryDuration.postValue(result);
        }
    }

    private class loadDailyActiveTimeAsyncTask extends AsyncTask<Void, Void, Long> {
        private SessionHelper sessionHelper;

        public loadDailyActiveTimeAsyncTask(SessionHelper sessionHelper) {
            this.sessionHelper = sessionHelper;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            try {
                return this.sessionHelper.getDailyActiveDuration();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {
            dailyActiveDuration.postValue(result);
        }
    }
}