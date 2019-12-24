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

    private Profile activeProfile;
    private MutableLiveData<ArrayList<Session>> sessions;
    private Session previousSession;
    private MutableLiveData<Session> pendingSession;

    private MutableLiveData<Integer> success;
    private MutableLiveData<Integer> streak;
    private MutableLiveData<Integer> finishedCount;

    private MutableLiveData<Long> dailySedentaryDuration;
    private MutableLiveData<Long> dailyActiveDuration;
    private MutableLiveData<Long> dailyVehicleDuration;
    private SessionHelper sessionHelper;

    private Handler tickHandler;
    private Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            tickHandler.postDelayed(timeUpdater, 1000);
            loadPendingSession();
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);
        ProfileHelper profileHelper = new ProfileHelper(this.getApplication());
        this.tickHandler = new Handler();

        try {
            this.activeProfile = profileHelper.getActive();
        } catch (SQLException e) {
            Toast.makeText(this.getApplication(), "Error, no profile", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        this.sessionHelper = new SessionHelper(this.getApplication(), activeProfile);

        startTicker();
    }

    public void updateModel() {
        loadDailyActiveTime();
        loadDailySedentaryTime();
        loadDailyVehicleTime();
        loadFinishedCount();
        // TODO: 12/23/19 now updated by handler ticker, should be made more elegant
//        loadPendingSession();
        loadSessions();
        loadStreak();
        loadSuccess();
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

    public Profile getActiveProfile() {
        return activeProfile;
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

    public LiveData<Session> getPendingSession() {
        if (this.pendingSession == null) {
            this.pendingSession = new MutableLiveData<Session>();
            loadPendingSession();
        }
        return this.pendingSession;
    }

    private void loadPendingSession() {
        new loadPendingSessionAsyncTask().execute();
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
        new loadDailySedentaryTimeAsyncTask().execute();
    }

    public LiveData<Long> getDailyActiveDuration() {
        if (this.dailyActiveDuration == null) {
            this.dailyActiveDuration = new MutableLiveData<Long>();
            loadDailyActiveTime();
        }
        return this.dailyActiveDuration;
    }

    private void loadDailyActiveTime() {
        new loadDailyActiveTimeAsyncTask().execute();
    }

    public MutableLiveData<Long> getDailyVehicleDuration() {
        if (this.dailyVehicleDuration == null) {
            this.dailyVehicleDuration = new MutableLiveData<Long>();
            loadDailyVehicleTime();
        }

        return this.dailyVehicleDuration;
    }

    private void loadDailyVehicleTime() {
        new loadDailyVehicleTimeAsyncTask().execute();
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

    private void loadSessions() {
        new loadSessionsAsyncTask().execute();
    }

    public void savePendingSession() {
        new savePendingSessionAsyncTask().execute();
    }

    public void discardPendingSession() {
        new discardPendingSessionAsyncTask().execute();
    }

    private class loadSessionsAsyncTask extends AsyncTask<Void, Void, ArrayList<Session>> {
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

    private class loadPendingSessionAsyncTask extends AsyncTask<Void, Void, Session> {
        @Override
        protected Session doInBackground(Void... voids) {
            try {
                return sessionHelper.getPending();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Session result) {
            if (previousSession == null) {
                previousSession = result;
            }

            if (!result.isEqual(previousSession)) {
                previousSession = result;
                updateModel();
            }

            pendingSession.postValue(result);
        }
    }

    private class loadDailySedentaryTimeAsyncTask extends AsyncTask<Void, Void, Long> {
        @Override
        protected Long doInBackground(Void... voids) {
            try {
                return sessionHelper.getDailySedentaryDuration();
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
        @Override
        protected Long doInBackground(Void... voids) {
            try {
                return sessionHelper.getDailyActiveDuration();
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

    private class loadDailyVehicleTimeAsyncTask extends AsyncTask<Void, Void, Long> {
        @Override
        protected Long doInBackground(Void... voids) {
            try {
                return sessionHelper.getDailyInVehicleDuration();
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

    private class savePendingSessionAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                sessionHelper.endPending();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class discardPendingSessionAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                sessionHelper.discardPending();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}