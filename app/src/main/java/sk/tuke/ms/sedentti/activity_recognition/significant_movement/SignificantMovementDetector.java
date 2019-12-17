package sk.tuke.ms.sedentti.activity_recognition.significant_movement;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Handler;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.notification.StopSedentaryNotification;

public class SignificantMovementDetector {
    private SensorManager sensorManager;
    private Sensor sensor;
    private Handler countDownHandler;
    private TriggerEventListener firstEventListener;
    private TriggerEventListener secondEventListener;
    private boolean firstMovement;
    private int countdown;
    private Context context;

    private Runnable movementStateMachineRunnable = new Runnable() {
        @Override
        public void run() {
            countdown -= Configuration.SIG_MOV_COUNTDOWN_UNIT;

            if (countdown <= 0) {
                firstMovement = false;
                sensorManager.cancelTriggerSensor(secondEventListener, sensor);
                sensorManager.requestTriggerSensor(firstEventListener, sensor);
                countdown = Configuration.SIG_MOV_TIMEOUT_TIME;
                // movement not recognized
            } else {
                countDownHandler.postDelayed(movementStateMachineRunnable, Configuration.SIG_MOV_COUNTDOWN_UNIT);
            }
        }
    };

    public SignificantMovementDetector(Context context) {
        this.context = context;
    }

    public void start() {
        this.firstMovement = false;
        this.countdown = Configuration.SIG_MOV_TIMEOUT_TIME;
        this.countDownHandler = new Handler();

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

        firstEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                firstMovement = true;
                countDownHandler.postDelayed(movementStateMachineRunnable, Configuration.SIG_MOV_COUNTDOWN_UNIT);
                sensorManager.requestTriggerSensor(secondEventListener, sensor);
            }
        };

        secondEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent triggerEvent) {
                if (firstMovement){
                    countDownHandler.removeCallbacks(movementStateMachineRunnable);
                    countdown = Configuration.SIG_MOV_TIMEOUT_TIME;
                    firstMovement = false;
                    new StopSedentaryNotification().createNotification(context,1);
                    stop();
                }
            }
        };
    }

    public void stop() {
        sensorManager.cancelTriggerSensor(firstEventListener, sensor);
        sensorManager.cancelTriggerSensor(secondEventListener, sensor);
    }
}
