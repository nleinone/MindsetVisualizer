package com.example.mindvisualizer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class CalibrationActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean calibrated = false;
    private AlertDialog successBox;
    List<String> dataBundleArray = new ArrayList<String>();
    private final String TAG = "CalibrationActivity";

    protected AlertDialog createAlertBoxWithButton(int calibSuccessT, int calibSuccessM)
    {

        String calibSuccessTitle = getString(calibSuccessT);
        String calibSuccessMsg = getString(calibSuccessM);

        final AlertDialog box = new AlertDialog.Builder(CalibrationActivity.this).create();
        box.setTitle(calibSuccessTitle);
        box.setMessage(calibSuccessMsg);
        box.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                box.cancel();
            }
        });

        return box;
    }

    public void UpdateTextViewValue(String text, TextView tv)
    {

        Log.d(TAG, "TextView Found");
        tv.setText(text);
        Log.d(TAG, "TextView set");
    }

    public void fastFourierTransform(String eeg1)
    {
        int eeg1Int = Integer.parseInt(eeg1);
        
    }

    public void updateEEGTextViews()
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("EegData", 0); // 0 - for private mode

        String eeg1 = pref.getString("eeg1String", null); // getting eeg1 from shared preference
        String eeg2 = pref.getString("eeg2String", null); // getting eeg2 from shared preference
        String eeg3 = pref.getString("eeg3String", null); // getting eeg3 from shared preference
        String eeg4 = pref.getString("eeg4String", null); // getting eeg4 from shared preference

        TextView eegTv1 = findViewById(R.id.eegChannel1Out);
        TextView eegtv2 = findViewById(R.id.eegChannel2Out);
        TextView eegtv3 = findViewById(R.id.eegChannel3Out);
        TextView eegtv4 = findViewById(R.id.eegChannel4Out);

        UpdateTextViewValue(eeg1, eegTv1);
        UpdateTextViewValue(eeg2, eegtv2);
        UpdateTextViewValue(eeg3, eegtv3);
        UpdateTextViewValue(eeg4, eegtv4);

        //Transform the raw EEG data to frequency:
        fastFourierTransform(eeg1);


    }

    public void updateUI()
    {
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                /* do what you need to do */
                Log.d(TAG, "UI updated");
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
        findViewById(R.id.return_prev).setOnClickListener(CalibrationActivity.this);


        //Get eeg data

        //Update UI:

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.start_calib) {
            // The user has pressed the "Start calibration" button.
            /*
             * code supposed to calibrate the headband.
             * if successful, will return calibrated = true
             * for the moment, we put it to true, in order to design the basic layers of the app
             */

            //Update EEG textviews every 5 seconds:
            updateUI();

            successBox = createAlertBoxWithButton(R.string.calibSuccessT, R.string.calibSuccessM);
            calibrated = true;
            successBox.show();

            //Collect EEG data to a data bundle. Needs a function which takes an instance of shared preference and appends a list with it. TODO
            //dataBundleArray = createDataBundleArray();

            //Update Firebase database with dataBundleArray. This works as a backup for the eeg data in case the muse disconnects.. TODO
            //

        }
        if (v.getId() == R.id.return_prev) {
            //if(calibrated) { //might not need this condition in the final code because the user might have a malfunctioning headband, therefore wanting to disconnect from this one and connect to another one
                Intent i = new Intent(CalibrationActivity.this, ConnectionActivity.class);
                i.putExtra("calib", calibrated);
                startActivity(i);
            //}
        }
    }

}
