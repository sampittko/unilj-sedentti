package sk.tuke.ms.sedentti.ui.history;

import android.app.Application;
import android.os.AsyncTask;

import java.sql.SQLException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.day.DayModel;
import sk.tuke.ms.sedentti.model.day.DayOverview;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.model.helper.day.DayOverviewHelper;

public class HistoryViewModel extends AndroidViewModel {

    private final DayOverviewHelper dayOverviewHelper;
    private MutableLiveData<ArrayList<DayModel>> dayModels;

    public HistoryViewModel(@NonNull Application application) {
        super(application);

        ProfileHelper profileHelper = new ProfileHelper(this.getApplication());
        Profile activeProfile = null;
        try {
            activeProfile = profileHelper.getActive();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SessionHelper sessionHelper = new SessionHelper(this.getApplication(), activeProfile);
        this.dayOverviewHelper = new DayOverviewHelper(sessionHelper);
    }

    public void updateModel() {
        loadDayModels();
    }

    public LiveData<ArrayList<DayModel>> getDayModels() {
        if (this.dayModels == null) {
            this.dayModels = new MutableLiveData<ArrayList<DayModel>>();
            loadDayModels();
        }
        return this.dayModels;
    }

    private void loadDayModels() {
        new loadDayModelsAsyncTask().execute();
    }

    private class loadDayModelsAsyncTask extends AsyncTask<Void, Void, ArrayList<DayOverview>> {
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