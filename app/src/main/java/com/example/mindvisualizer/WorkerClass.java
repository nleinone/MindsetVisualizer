package com.example.mindvisualizer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WorkerClass extends Worker {

    public WorkerClass(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        Log.v("Worker", "Work started!");
        String calibrationMode = "0";
        setCalibrationMode(calibrationMode);
        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }

    private void setCalibrationMode(String calibMode) {
        //Set calibration mode to off (0)
        SharedPreferences prefCalibrationMode = getApplicationContext().getSharedPreferences("prefCalibrationMode", 0); // 0 - for private mode
        prefCalibrationMode.edit().putString("CalibrationMode", calibMode).apply();
    }

}