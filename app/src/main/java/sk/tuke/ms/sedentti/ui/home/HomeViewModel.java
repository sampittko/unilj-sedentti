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


    public LiveData<ArrayList<Session>> getHomeTimelineSessions() {
        if (sessions == null) {
            sessions = new MutableLiveData<ArrayList<Session>>();
            loadSessions();
        }
        return sessions;
    }

    private void loadSessions() {
        new loadAsyncTask(this.sessionHelper).execute();
    }

    private class loadAsyncTask extends AsyncTask<Void, Void, ArrayList<Session>> {

        private SessionHelper sessionHelper;

        loadAsyncTask(SessionHelper sessionHelper) {
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

}