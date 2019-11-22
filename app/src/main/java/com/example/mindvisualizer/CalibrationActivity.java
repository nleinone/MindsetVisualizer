package com.example.mindvisualizer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class CalibrationActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean calibrated = false;
    private AlertDialog success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        findViewById(R.id.start_calib).setOnClickListener(CalibrationActivity.this);
        findViewById(R.id.return_prev).setOnClickListener(CalibrationActivity.this);

        final AlertDialog success = new AlertDialog.Builder(CalibrationActivity.this).create();
        success.setTitle(getString(R.string.calibSuccessT));
        success.setMessage(getString(R.string.calibSuccessM));
        success.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                success.cancel();
            }
        });
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
            success.show();
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
