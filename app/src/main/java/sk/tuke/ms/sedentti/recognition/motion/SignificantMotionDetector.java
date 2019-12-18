package sk.tuke.ms.sedentti.recognition.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Handler;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import sk.tuke.ms.sedentti.config.Configuration;

public class SignificantMotionDetector {
    private static final String TAG = "SignificantMotionDetector";
    private SensorManager sensorManager;
    private Sensor sensor;
    private Handler countDownHandler;
    private TriggerEventListener firstEventListener;
    private TriggerEventListener secondEventListener;
    private boolean firstMovement;
    private int countdown;
    private Context context;
    private SignificantMotionListener significantMotionListener;
    private boolean hasDetectionStarted;

    private Runnable movementStateMachineRunnable = new Runnable() {
        @Override
        public void run() {
            countdown -= Configuration.SIG_MOV_COUNTDOWN_UNIT;

            if (countdown <= 0) {
                // movement not recognized
                firstMovement = false;
                sensorManager.cancelTriggerSensor(secondEventListener, sensor);
                sensorManager.requestTriggerSensor(firstEventListener, sensor);
                countdown = Configuration.SIG_MOV_TIMEOUT_TIME;
            } else {
                countDownHandler.postDelayed(movementStateMachineRunnable, Configuration.SIG_MOV_COUNTDOWN_UNIT);
            }
        }
    };

    public SignificantMotionDetector(@NotNull Context context, SignificantMotionListener significantMotionListener) {
        this.context = context;
        this.significantMotionListener = significantMotionListener;
        this.hasDetectionStarted = false;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
    }

    public void start() {
        if (hasDetectionStarted) {
            Crashlytics.log(Log.DEBUG, TAG, "Detection has already started");
            return;
        }
        Crashlytics.log(Log.DEBUG, TAG, "Starting detection");
        initializeValues();
        setEventListeners();
        toggleDetectionState();
    }

    private void initializeValues() {
        Crashlytics.log(Log.DEBUG, TAG, "Initializing values");
        firstMovement = false;
        countdown = Configuration.SIG_MOV_TIMEOUT_TIME;
        countDownHandler = new Handler();
    }

    private void setEventListeners() {
        Crashlytics.log(Log.DEBUG, TAG, "Setting event listeners");

        firstEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
//                first movement detected
//                lets detect second one
                firstMovement = true;
                countDownHandler.postDelayed(movementStateMachineRunnable, Configuration.SIG_MOV_COUNTDOWN_UNIT);
                sensorManager.requestTriggerSensor(secondEventListener, sensor);
            }
        };

        secondEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent triggerEvent) {
                if (firstMovement){
//                    check whether we are in limit
//                    second movement detected here
//                    reset values
                    countDownHandler.removeCallbacks(movementStateMachineRunnable);
                    countdown = Configuration.SIG_MOV_TIMEOUT_TIME;
                    firstMovement = false;

//                    fire callback
                    significantMotionListener.onSignificantMotionDetected();
                    stop();
                }
            }
        };
    }

    public void stop() {
        if (!hasDetectionStarted) {
            Crashlytics.log(Log.DEBUG, TAG, "Detection has already stopped");
            return;
        }
        Crashlytics.log(Log.DEBUG, TAG, "Stopping detection");
        sensorManager.cancelTriggerSensor(firstEventListener, sensor);
        sensorManager.cancelTriggerSensor(secondEventListener, sensor);
        toggleDetectionState();
    }

    private void toggleDetectionState() {
        hasDetectionStarted = !hasDetectionStarted;
    }

    public boolean isDetecting() {
        return hasDetectionStarted;
    }
}
