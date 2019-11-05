package sk.tuke.ms.sedentti;

import android.os.Bundle;

import com.facebook.stetho.Stetho;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import sk.tuke.ms.sedentti.activity.recognition.ActivityRecognitionHandler;
import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityRecognitionHandler activityRecognitionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_statistics, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Stetho.initializeWithDefaults(this);

//        Intent intent = new Intent(this, ActivityRecognitionService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        } else {
//            startService(intent);
//        }


        addToDb();
    }

    private void addToDb() {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);

        Dao<Activity, Long> activityDao = null;

        try {
            activityDao = databaseHelper.activityDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();

        activity.setActivityType(1);
        activity.setTransitionType(1);
        activity.setElapsedRealTimeNanos(1);

        try {
            activityDao.create(activity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
