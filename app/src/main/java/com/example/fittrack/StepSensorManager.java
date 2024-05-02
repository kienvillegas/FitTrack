package com.example.fittrack;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class StepSensorManager {
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private StepDetector stepDetector;
    private int stepCount = 0;
    private StepCountListener stepCountListener;

    public interface StepCountListener {
        void onStepCount(int stepCount);
    }

    public StepSensorManager(Context context, StepCountListener listener) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        stepDetector = new StepDetector();
        this.stepCountListener = listener;
    }

    public void registerListener() {
        if (stepSensor != null) {
            Log.e("StepSensorManager", "StepSensor Registered");
            sensorManager.registerListener(stepDetector, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("StepSensorManager", "Step sensor is not available on this device");
        }
    }

    public void unregisterListener() {
        Log.e("StepSensorManager", "StepSensor Unregistered");
        sensorManager.unregisterListener(stepDetector);
    }

    public int getStepCount(){
        return stepCount;
    }

    public void setStepCount(int stepCount){
        this.stepCount = stepCount;

        if(stepCountListener != null){
            stepCountListener.onStepCount(stepCount);
        }
    }


    private class StepDetector implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // Increment step count whenever a step is detected
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                stepCount++;
                // Notify listener about the updated step count
                if (stepCountListener != null) {
                    stepCountListener.onStepCount(stepCount);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Handle accuracy changes if needed
        }
    }
}
