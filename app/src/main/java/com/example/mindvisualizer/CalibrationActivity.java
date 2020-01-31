package com.example.mindvisualizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.google.firebase.database.core.view.Change;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CalibrationActivity extends AppCompatActivity implements View.OnClickListener {

    List<String> dataBundleArray = new ArrayList<String>();
    private final String TAG = "CalibrationActivity";
    String workTag = "CalibrationSession";

    public void UpdateTextViewValue(String text, TextView tv)
    {

        Log.d(TAG, "TextView Found");
        tv.setText(text);
        Log.d(TAG, "TextView set");
    }

    //public void fastFourierTransform(String eeg1)
    //{
    //    int eeg1Int = Integer.parseInt(eeg1);
    //
    //}

    public void updateEEGTextViews()
    {
        Log.v("CalibrationActivity", "Data received.");
        /*
        SharedPreferences pref = getApplicationContext().getSharedPreferences("EegData", 0); // 0 - for private mode

        String eeg1 = pref.getString("eeg1String", null); // getting eeg1 from shared preference
        String eeg2 = pref.getString("eeg2String", null); // getting eeg2 from shared preference
        String eeg3 = pref.getString("eeg3String", null); // getting eeg3 from shared preference
        String eeg4 = pref.getString("eeg4String", null); // getting eeg4 from shared preference

        UpdateTextViewValue(eeg1, eegTv1);
        UpdateTextViewValue(eeg2, eegtv2);
        UpdateTextViewValue(eeg3, eegtv3);
        UpdateTextViewValue(eeg4, eegtv4);

        //Transform the raw EEG data to frequency:
        //fastFourierTransform(eeg1);
        */
    }

    public void updateUI()
    {
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                /* do what you need to do */
                //Log.d(TAG, "UI updated");

                updateEEGTextViews();
                /* and here comes the "trick" */
                handler.postDelayed(this, 5000);
            }
        };

        handler.postDelayed(runnable, 5000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        findViewById(R.id.start_calib).setOnClickListener(CalibrationActivity.this);

        SharedPreferences prefCalibrationMode = getApplicationContext().getSharedPreferences("prefCalibrationMode", 0); // 0 - for private mode

        //Add Shared preference key to the listener list
        prefCalibrationMode.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

    }

    //Do stuff when key is changed
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("CalibrationMode")){

                TextView statusTv = findViewById(R.id.status);

                String value = sharedPreferences.getString(key, "nothing");
                if(value.equals("0"))
                {
                    statusTv.setText(R.string.xml_calib);
                }
                else if(value.equals("1"))
                {
                    statusTv.setText(R.string.calibrating);
                }
                else
                {
                    statusTv.setText(R.string.calibSuccessM);
                    Intent intent = new Intent(CalibrationActivity.this , VisualActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    };

    @Override
    public void onClick(View v) {

        SharedPreferences prefCalibrationMode = getApplicationContext().getSharedPreferences("prefCalibrationMode", 0);
        Log.v("CalibrationActivity", "Click");
        if (v.getId() == R.id.start_calib) {

            Log.v("CalibrationActivity", "Calibration started");
            //Increase sessionId by 1
            String sessionId = prefCalibrationMode.getString("SessionId", "0");
            int intSessionId = Integer.parseInt(sessionId);
            intSessionId += 1;
            sessionId = Integer.toString(intSessionId);
            prefCalibrationMode.edit().putString("SessionId", sessionId).apply();

            //Change calibration mode on (1 = True, 0 = False, Visualization mode = 2:
            String calibMode = "1";
            prefCalibrationMode.edit().putString("CalibrationMode", calibMode).apply();

            Log.v("CalibrationActivity", "CalibrationMode changed: " + calibMode);
            EditText durationOfCalibrationET = findViewById(R.id.calibDurationEditText);

            int durationOfCalibration = 15000; //ms

            if (!durationOfCalibrationET.getText().toString().equals(""))
            {
                durationOfCalibration = Integer.parseInt(durationOfCalibrationET.getText().toString()) * 1000;
                Log.v("CalibrationActivity", "Duration set to: " + durationOfCalibration);
            }

            //The workManager will turn the calibration mode 2 after the duration of the Calibration
            OneTimeWorkRequest calibrationWork = new OneTimeWorkRequest.Builder(WorkerClass.class)
                    .setInitialDelay(durationOfCalibration, TimeUnit.MILLISECONDS)
                    .addTag(workTag + sessionId)
                    .build();

            WorkManager.getInstance(this).enqueue(calibrationWork);
            Log.v("CalibrationActivity", "Work queued: " + workTag + sessionId);
            updateUI();
        }
    }

}
