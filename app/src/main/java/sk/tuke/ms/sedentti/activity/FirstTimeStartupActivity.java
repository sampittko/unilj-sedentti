package sk.tuke.ms.sedentti.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;

import org.jetbrains.annotations.NotNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;

public class FirstTimeStartupActivity extends AppCompatActivity {

    private static final String TAG = "FTStartupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // line that needs to be run after database scheme upgrade (firstly change version FROM and version TO)
//         DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
//         databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), databaseHelper.getConnectionSource(), 5, 6);

        Stetho.initializeWithDefaults(this);

        checkSigMovSensor();

        if (permissionsGranted()) {
            Crashlytics.log(Log.DEBUG, TAG, "Permissions already granted");
            decideNextStep();
        } else {
            Crashlytics.log(Log.DEBUG, TAG, "Requesting missing permissions");
            requestPermissions();
        }
    }

    private void checkSigMovSensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        if (sensor == null) {
            Toast.makeText(this, "Your phone is not supported by this app due to missing sensors", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean permissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PredefinedValues.PERMISSION_REQUEST_CODE_READ_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PredefinedValues.PERMISSION_REQUEST_CODE_READ_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Crashlytics.log(Log.DEBUG, TAG, "Permissions were granted successfully");
                decideNextStep();
            } else {
                Crashlytics.log(Log.ERROR, TAG, "Permissions were not granted by the user, quitting app as a consequence");
                finish();
            }
        }
    }

    private void decideNextStep() {
        AppSPHelper appSPHelper = new AppSPHelper(this);
        appSPHelper.setAppDefaultSettings();

        if (appSPHelper.firstTimeStartupPerformed()) {
            Crashlytics.log(Log.DEBUG, TAG, "First time startup had already been performed before");

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Crashlytics.log(Log.DEBUG, TAG, "Performing first time startup now");

            setContentView(R.layout.activity_first_time_startup);

            // TODO implement
            appSPHelper.updateFirstTimeStartupPerformed(true);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
