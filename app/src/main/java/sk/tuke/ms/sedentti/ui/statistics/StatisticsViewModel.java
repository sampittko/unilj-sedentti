package sk.tuke.ms.sedentti.ui.statistics;

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
import sk.tuke.ms.sedentti.model.day.DayModel;
import sk.tuke.ms.sedentti.model.day.DayOverview;
import sk.tuke.ms.sedentti.model.helper.DayOverviewHelper;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class StatisticsViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<Session>> sessions;

    private final DayOverviewHelper dayOverviewHelper;
    private MutableLiveData<ArrayList<DayModel>> dayModels;
    private SessionHelper sessionHelper;

    public StatisticsViewModel(@NonNull Application application) {
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
        this.dayOverviewHelper = new DayOverviewHelper(sessionHelper);
    }


    public LiveData<ArrayList<Session>> getSessions() {
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
                return sessionHelper.getLatestSessions();
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

    public LiveData<ArrayList<DayModel>> getDayModels() {
        if (this.dayModels == null) {
            this.dayModels = new MutableLiveData<ArrayList<DayModel>>();
            loadDayModels();
        }
        return this.dayModels;
    }

    private void loadDayModels() {
        new loadDayModelsAsyncTask(this.sessionHelper).execute();
    }

    private class loadDayModelsAsyncTask extends AsyncTask<Void, Void, ArrayList<DayOverview>> {

        private SessionHelper sessionHelper;

        public loadDayModelsAsyncTask(SessionHelper sessionHelper) {
            this.sessionHelper = sessionHelper;
        }

        @Override
        protected ArrayList<DayOverview> doInBackground(Void... voids) {
            try {
                return dayOverviewHelper.getDayOverviews();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<DayOverview> result) {
            ArrayList<DayModel> list = new ArrayList<>();

            for (DayOverview dayOverview : result) {
                list.add(dayOverview.getDay());
                list.addAll(dayOverview.getSessionsOfDay());
            }
            dayModels.postValue(list);
        }
    }
}