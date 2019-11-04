package sk.tuke.ms.sedentti;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import sk.tuke.ms.sedentti.activity.recognition.ActivityRecognitionHandler;

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

        activityRecognitionHandler = new ActivityRecognitionHandler(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        activityRecognitionHandler.startTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();

        activityRecognitionHandler.stopTracking();
    }
}
