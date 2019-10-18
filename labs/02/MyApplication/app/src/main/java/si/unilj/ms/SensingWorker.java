package si.unilj.ms;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aware.Accelerometer;
import com.aware.Aware;
//import com.aware.Aware_Preferences;
import com.aware.providers.Accelerometer_Provider;

public class SensingWorker extends Worker {
    private final String TAG = "SensingWorker";
    private final Context context;

    public SensingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

//        Aware.startAWARE(context);
//
//        //sampling frequency in microseconds
//        Aware.setSetting(context, Aware_Preferences.FREQUENCY_ACCELEROMETER, 200000);
//
//        // intensity threshold to report the reading
//        Aware.setSetting(context, Aware_Preferences.THRESHOLD_ACCELEROMETER, 0.02f);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "I am here");

        Aware.startAccelerometer(context);

        Accelerometer.setSensorObserver(new Accelerometer.AWARESensorObserver() {
            @Override
            public void onAccelerometerChanged(ContentValues data) {
                final double x = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0);
                final double y = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1);
                final double z = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2);

                final double timestamp = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.TIMESTAMP);

                Log.d(TAG, timestamp + "x: " + x + "y: " + y + "z: " + z);
            }
        });
        return Result.success();
    }
}
