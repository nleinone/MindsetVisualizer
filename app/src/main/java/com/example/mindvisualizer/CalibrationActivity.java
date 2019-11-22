package com.example.mindvisualizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CalibrationActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean calibrated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.start_calib) {
            // The user has pressed the "Start calibration" button.
            // For the moment we have no calibration designed. TODO
            /*
             * code supposed to calibrate the headband.
             * if successful, will return calibrated = true
             * for the moment, we put it to true, in order to design the basic layers of the app
             */

            calibrated = true;
            if(calibrated) {
                Intent i = new Intent(CalibrationActivity.this, ConnectionActivity.class);
                i.putExtra("calib", calibrated);
                startActivity(i);
            }
        }
    }
}
