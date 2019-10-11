package si.unilj.ms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Temperature;
import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Temperature_Provider;

import java.util.concurrent.TimeUnit;

// TRY FOREGROUND SERVICE IF WORKER FAILS

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    @Override
    protected void onStart() {
        super.onStart();

//        Aware.startAWARE(this);
//
//        //sampling frequency in microseconds
//        Aware.setSetting(this, Aware_Preferences.FREQUENCY_ACCELEROMETER, 200000);
//
//        // intensity threshold to report the reading
//        Aware.setSetting(this, Aware_Preferences.THRESHOLD_ACCELEROMETER, 0.02f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WorkManager mWorkManager =
                WorkManager.getInstance(getApplicationContext());
        PeriodicWorkRequest.Builder myWorkBuilder =
                new PeriodicWorkRequest.Builder(SensingWorker.class, 1, TimeUnit.SECONDS);
        PeriodicWorkRequest myWork = myWorkBuilder.build();
        mWorkManager.enqueueUniquePeriodicWork("sensingJob",
                ExistingPeriodicWorkPolicy.KEEP, myWork);

//        Accelerometer.setSensorObserver(new Accelerometer.AWARESensorObserver() {
//            @Override
//            public void onAccelerometerChanged(ContentValues data) {
//                final double x = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0);
//                final double y = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1);
//                final double z = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2);
//
//                final double timestamp = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.TIMESTAMP);
//
//                Log.d(TAG, timestamp + "x: " + x + "y: " + y + "z: " + z);
//
//                runOnUiThread(new Runnable() {
//                    private TextView tw1, tw2, tw3;
//
//                    @Override
//                    public void run() {
//                        tw1 = findViewById(R.id.tv_acc_x_value);
//                        tw1.setText(Double.toString(x));
//                        tw2 = findViewById(R.id.tv_acc_y_value);
//                        tw2.setText(Double.toString(y));
//                        tw3 = findViewById(R.id.tv_acc_z_value);
//                        tw3.setText(Double.toString(z));
//                    }
//                });
//            }
//        });
//
//        Temperature.setSensorObserver(new Temperature.AWARESensorObserver() {
//            @Override
//            public void onTemperatureChanged(ContentValues data) {
//                final double temperature = data.getAsDouble(Temperature_Provider.Temperature_Data.TEMPERATURE_CELSIUS);
//
//                runOnUiThread(new Runnable() {
//                    private TextView tw;
//
//                    @Override
//                    public void run() {
//                        tw = findViewById(R.id.tv_temp_value);
//                        tw.setText(Double.toString(temperature));
//                    }
//                });
//            }
//        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Aware.startAccelerometer(this);
//        Aware.startTemperature(this);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Aware.stopAccelerometer(this);
//        Aware.stopTemperature(this);
//    }
}
