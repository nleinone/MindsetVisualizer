package com.example.mindvisualizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.choosemuse.libmuse.Accelerometer;
import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Calendar;
import java.util.Map;

public class ConnectionActivity extends AppCompatActivity implements View.OnClickListener {
    private Button calib;
    //Eeg value update init variable

    /**
     * Tag used for logging purposes.
     */
    private final String TAG1 = "EEG";
    private final String TAG2 = "REL_ALPHA";
    private final String TAG3 = "ABS_ALPHA";
    int eegSnapShotCounter;
    List<Float> avgs = new ArrayList<>();
    /**
     * The MuseManager is how you detect Muse headbands and receive notifications
     * when the list of available headbands changes.
     */
    private MuseManagerAndroid manager;

    /**
     * A Muse refers to a Muse headband.  Use this to connect/disconnect from the
     * headband, register listeners to receive EEG data and get headband
     * configuration and version information.
     */
    private Muse muse;


    /**
     * In the UI, the list of Muses you can connect to is displayed in a Spinner object for this example.
     * This spinner adapter contains the MAC addresses of all of the headbands we have discovered.
     */
    private ArrayAdapter<String> spinnerAdapter;

    /**
     * It is possible to pause the data transmission from the headband.  This boolean tracks whether
     * or not the data transmission is enabled as we allow the user to pause transmission in the UI.
     */
    private boolean dataTransmission = true;

    // UI elements
    Spinner spinner;
    TextView status;
    int eegSnapShotCounterRawData = 0;
    int eegSnapShotCounterAlpha = 0;
    private boolean calibrated;
    private double alphaMean = 0;
    int eegSnapShotResetCounter;
    float tempValue = 0;


    public void UpdateTextViewValue(String text, TextView tv)
    {

        Log.d(TAG1, "TextView Found");
        tv.setText(text);
        Log.d(TAG1, "TextView set");
    }

    public void uploadValuesToFirebase(String sessionId, float avgValue, String waveName)
    {
        //Reference to root node:
        Log.v("ConnectionActivity", "t1");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        Log.v("ConnectionActivity", "t2");
        DatabaseReference firebaseRootReference = database.getReference("musefirebase-79096");
        Log.v("ConnectionActivity", "t3");
        //Create new node for the current calibration session
        DatabaseReference sessionReference = firebaseRootReference.child("Session-" + sessionId);
        Log.v("ConnectionActivity", "t4");

        //Create patch id:
        SharedPreferences prefPatchId = getApplicationContext().getSharedPreferences("prefPatchId", 0); // 0 - for private mode
        Log.v("ConnectionActivity", "t6");
        String patchId = prefPatchId.getString("PatchId", "0");
        Log.v("ConnectionActivity", "t7");
        int intPatchId = Integer.parseInt(patchId);
        Log.v("ConnectionActivity", "t8");
        intPatchId += 1;
        Log.v("ConnectionActivity", "t9");
        patchId = Integer.toString(intPatchId);
        Log.v("ConnectionActivity", "t10");
        prefPatchId.edit().putString("PatchId", patchId).apply();
        Log.v("ConnectionActivity", "t11");

        sessionReference.child("Patch-" + patchId).setValue(avgValue);
        //Insert average value to this session:
        Log.v("ConnectionActivity", "Saved SessionId: " + sessionId);
        Log.v("ConnectionActivity", "Saved avgValue: " + avgValue);
        Log.v("ConnectionActivity", "Saved waveName: " + waveName);
    }

    public double convertNaNtoZero(double eeg)
    {
        double value = 0;
        if(Double.isNaN(eeg))
        {
            return value;
        }
        else
        {
            return eeg;
        }
    }

    public void uploadEEGValueToSharedRef(MuseDataPacket p, int timeRate, String waveName)
    {

        //list for firebase database avg values


        double eeg1 = p.getEegChannelValue(Eeg.EEG1);
        double eeg2 = p.getEegChannelValue(Eeg.EEG2);
        double eeg3 = p.getEegChannelValue(Eeg.EEG3);
        double eeg4 = p.getEegChannelValue(Eeg.EEG4);
        double aux_l = p.getEegChannelValue(Eeg.AUX_LEFT);
        double aux_r = p.getEegChannelValue(Eeg.AUX_RIGHT);

        //What to do with NaNs:
        eeg1 = convertNaNtoZero(eeg1);
        eeg2 = convertNaNtoZero(eeg2);
        eeg3 = convertNaNtoZero(eeg3);
        eeg4 = convertNaNtoZero(eeg4);

        double avgEEGValue = ((eeg1 + eeg2 + eeg3 + eeg4) / 4);
        float avgEEGValueFloat = (float) avgEEGValue;
        Log.v("ConnectionActivity", "avgEEGValueFloat: " + avgEEGValueFloat);

        eegSnapShotCounter += 1;
        Log.v("Timers", "eegSnapShotCounter: " + eegSnapShotCounter);

        //Reset shared reference every 5 counter:
        SharedPreferences pref = getApplicationContext().getSharedPreferences("EegData", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        SharedPreferences prefCalibrationMode = getApplicationContext().getSharedPreferences("prefCalibrationMode", 0); // 0 - for private mode

        //Muse eeg data is updated roughly 3 times every 1 millisecond. This is just an estimate without any specific calculations.

        if (eegSnapShotCounter == timeRate)
        {
            eegSnapShotCounter = 0;
            eegSnapShotResetCounter += 1;
            Log.v("Timers", "eegSnapShotResetCounter: " + eegSnapShotResetCounter);
            //Add avg to list every 10th data packet.
            SharedPreferences prefEeg = getApplicationContext().getSharedPreferences("avgEEG", 0);
            prefEeg.edit().putFloat("averageEEG", avgEEGValueFloat).apply();

            avgs.add((float)avgEEGValueFloat);
            if(eegSnapShotResetCounter == 5)
            {
                eegSnapShotResetCounter = 0;

                //Update eeg to sharedPreference:

                //Update avg patch to firebase

                for (int i = 0; i < avgs.size(); i++) {

                    float singleAvgValue = avgs.get(i);
                    tempValue += singleAvgValue;
                    Log.v("ConnectionActivity", "avgs.get(i): " + avgs.get(i));
                    Log.v("ConnectionActivity", "len: " + avgs.size());
                    Log.v("ConnectionActivity", "i: " + i);
                    Log.v("ConnectionActivity", "singleAvgValue: " + singleAvgValue);
                    Log.v("ConnectionActivity", "TempValue: " + tempValue);
                    if(i == avgs.size() - 1)
                    {
                        Log.v("ConnectionActivity", "TempValue: " + tempValue);
                        //UPDATE THIS TO FIREBASE HERE
                        float fireBaseUpdateValue = tempValue / avgs.size();
                        tempValue = 0;
                        avgs = new ArrayList<>();
                        Log.v("ConnectionActivity", "TempValue after zero: " + tempValue);
                        //Check if the calibration Mode is on, if On, save data to firebase, if not, dont:
                        String calibrationMode = prefCalibrationMode.getString("CalibrationMode", "0");


                        Log.v("ConnectionActivity", "CalibrationMode: " + calibrationMode);
                        Log.v("ConnectionActivity", "Saved AVG value: " + fireBaseUpdateValue);
                        if(calibrationMode.equals("1"))
                        {
                            String sessionId = prefCalibrationMode.getString("SessionId", "0");
                            Log.v("ConnectionActivity", "SessionId: " + sessionId);
                            uploadValuesToFirebase(sessionId, fireBaseUpdateValue, waveName);
                            Log.v("ConnectionActivity", "Uploaded to firebase!");
                            //Upload to firebase
                        }

                        //After the update clean the phone memory (shared preference)
                        Log.d(TAG2, "Data cleared");
                        pref.edit().clear().apply();
                    }
                }



            }

            if(p.packetType() == MuseDataPacketType.ALPHA_RELATIVE)
            {
                Log.d(TAG2, "EEG rel alpha average: " + avgEEGValue);
                Log.d(TAG2, "EEG rel alphas: " + eeg1 + " " + eeg2 + " " + eeg3
                        + " " + eeg4 + " " + aux_l + " " + aux_r);
            }

            else if(p.packetType() == MuseDataPacketType.EEG)
            {
                Log.d(TAG1, "EEG average: " + avgEEGValue);
                Log.d(TAG1, "EEG: " + eeg1 + " " + eeg2 + " " + eeg3
                        + " " + eeg4 + " " + aux_l + " " + aux_r);
            }

            else if(p.packetType() == MuseDataPacketType.ALPHA_ABSOLUTE)
            {
                Log.d(TAG3, "EEG abs average: " + avgEEGValue);
                Log.d(TAG3, "EEG abs alphas: " + eeg1 + " " + eeg2 + " " + eeg3
                        + " " + eeg4 + " " + aux_l + " " + aux_r);
            }

            //Update EEG value to all activities it is used. Right now only shown in visual activity, and the data collections starts when the Muse is connected.
            //Check if VisualActivity exists (The activity is opened once)
            /*https://www.journaldev.com/9412/android-shared-preferences-example-tutorial*/

            String avgEEGValueString = Double.toString(avgEEGValue);
            String eeg1String = Double.toString(eeg1);
            String eeg2String = Double.toString(eeg2);
            String eeg3String = Double.toString(eeg3);
            String eeg4String = Double.toString(eeg4);
            String aux_lString = Double.toString(aux_l);
            String aux_rString = Double.toString(aux_r);

            TextView eegTv1;

            if(p.packetType() == MuseDataPacketType.ALPHA_RELATIVE)
            {
                //Save EEG channel data to shared data section of the mobile phone:
                editor.putString("eegAlpha1String", eeg1String); // Storing string
                editor.putString("eegAlpha2String", eeg2String); // Storing string
                editor.putString("eegAlpha3String", eeg3String); // Storing string
                editor.putString("eegAlpha4String", eeg4String); // Storing string
                editor.putString("aux_lAlphaString", aux_lString); // Storing string
                editor.putString("aux_rAlphaString", aux_rString); // Storing string

                editor.putString("avgEegAlphaString", avgEEGValueString); // Storing string

                eegTv1 = findViewById(R.id.avgAlpha1);
                UpdateTextViewValue(avgEEGValueString, eegTv1);
            }

            else if(p.packetType() == MuseDataPacketType.ALPHA_ABSOLUTE)
            {
                //Save EEG channel data to shared data section of the mobile phone:
                editor.putString("eeg1AbsAlphaString", eeg1String); // Storing string
                editor.putString("eeg2AbsAlphaString", eeg2String); // Storing string
                editor.putString("eeg3AbsAlphaString", eeg3String); // Storing string
                editor.putString("eeg4AbsAlphaString", eeg4String); // Storing string
                editor.putString("aux_lAbsAlphaString", aux_lString); // Storing string
                editor.putString("aux_rAbsAlphaString", aux_rString); // Storing string
                editor.putString("avgEegAlphaString", avgEEGValueString); // Storing string
            }
            else
            {
                //Save EEG channel data to shared data section of the mobile phone:
                editor.putString("eeg1String", eeg1String); // Storing string
                editor.putString("eeg2String", eeg2String); // Storing string
                editor.putString("eeg3String", eeg3String); // Storing string
                editor.putString("eeg4String", eeg4String); // Storing string
                editor.putString("aux_lString", aux_lString); // Storing string
                editor.putString("aux_rString", aux_rString); // Storing string
            }

            editor.commit();

            //Upload shared preferences



        }
    }

    public void ConnectAndStreamMuseData(List<Muse> availableMuses)
    {
        MuseDataListener dataListener = new MuseDataListener() {

            /**
             * You will receive a callback to this method each time the headband sends a MuseDataPacket
             * that you have registered.  You can use different listeners for different packet types or
             * a single listener for all packet types as we have done here.
             * @param p     The data packet containing the data from the headband (eg. EEG data)
             * @param muse  The headband that sent the information.
             */
        @Override
        public void receiveMuseDataPacket(MuseDataPacket p, Muse muse) {
            switch (p.packetType()) {
                //case EEG:
                //    uploadEEGValueToSharedRef(p, 500);
                //    break;
                case ALPHA_RELATIVE:
                    String waveName = "AlphaRelative";
                    int timeRate = 5;
                    uploadEEGValueToSharedRef(p, timeRate, waveName);
                    break;

                //case ALPHA_ABSOLUTE:
                //    String waveName = "AlphaAbsolute";

                //uploadEEGValueToSharedRef(p, 5, waveName);
                //   break;

                default:
                    break;
            }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket museArtifactPacket, Muse muse) {
        }
    };

        // Cache the Muse that the user has selected.
        muse = availableMuses.get(spinner.getSelectedItemPosition());
        // Unregister all prior listeners and register our data listener to
        // receive the MuseDataPacketTypes we are interested in.  If you do
        // not register a listener for a particular data type, you will not
        // receive data packets of that type.
        muse.unregisterAllListeners();
        muse.registerConnectionListener(connectionListener);
        muse.registerDataListener(dataListener, MuseDataPacketType.EEG);
        muse.registerDataListener(dataListener, MuseDataPacketType.ALPHA_RELATIVE);
        muse.registerDataListener(dataListener, MuseDataPacketType.ALPHA_ABSOLUTE);

        // Initiate a connection to the headband and stream the data asynchronously.
        muse.runAsynchronously();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {   // Niko without onCreate method i cannot push the value to the database


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("EEG_value");

        myRef.setValue("246");



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Clear shared reference data when the app is launched.
        SharedPreferences pref = getApplicationContext().getSharedPreferences("EegData", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.clear().apply();

        Log.d(TAG1, "Cleared shared reference file");


        Bundle b = getIntent().getExtras();
        calibrated = b.getBoolean("calib");

        // Assign click event listener to buttons
        findViewById(R.id.scan).setOnClickListener(this);
        findViewById(R.id.connect).setOnClickListener(this);
        findViewById(R.id.disconnect).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.calib).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);

        // Initialize spinner
        spinnerAdapter = new ArrayAdapter<>(ConnectionActivity.this, android.R.layout.simple_spinner_item);
        spinner = findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        status = findViewById(R.id.status);

        // We need to set the context on MuseManagerAndroid before we can do anything.
        // This must come before other LibMuse API calls as it also loads the library.
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(this);

        // Register a listener to receive notifications of what Muse headbands
        // we can connect to.
        manager.setMuseListener(museListener);

        // Request permissions for devices with API 23+
        ensurePermissions();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.scan) {
            // The user has pressed the "Scan" button.
            // Start listening for nearby or paired Muse headbands. We call stopListening
            // first to make sure startListening will clear the list of headbands and start fresh.
            manager.stopListening();
            manager.startListening();

        } else if (v.getId() == R.id.connect) {

            // The user has pressed the "Connect" button to connect to
            // the headband in the spinner.

            // Listening is an expensive operation, so now that we know
            // which headband the user wants to connect to we can stop
            // listening for other headbands.
            manager.stopListening();

            List<Muse> availableMuses = manager.getMuses();

            // Check that we actually have something to connect to.
            if (availableMuses.size() < 1 || spinner.getAdapter().getCount() < 1) {
                Toast.makeText(getApplicationContext(), R.string.nothing, Toast.LENGTH_SHORT).show();
                Log.d(TAG1, "There is nothing to connect to");
            } else {

                ConnectAndStreamMuseData(availableMuses);

            }

        }

        else if (v.getId() == R.id.disconnect) {

            // The user has pressed the "Disconnect" button.
            // Disconnect from the selected Muse.
            if (muse != null) {
                muse.disconnect();
                spinnerAdapter.clear();
            } else {
                Toast.makeText(getApplicationContext(), R.string.toast_disconnect_exception, Toast.LENGTH_SHORT).show();
            }

        }

        else if (v.getId() == R.id.pause) {

            // The user has pressed the "Pause/Resume" button to either pause or
            // resume data transmission.  Toggle the state and pause or resume the
            // transmission on the headband.
            if (muse != null) {
                dataTransmission = !dataTransmission;
                muse.enableDataTransmission(dataTransmission);
            }
        }

        else if (v.getId() == R.id.calib) {

            // The user has pressed the "Calibration" button.
            // We must verify that the device is connected to a Muse device before starting the calibration.
            // If the device is connected, then we can switch to the calibration activity.
            /*if (muse != null) {
                startActivity(new Intent(ConnectionActivity.this, CalibrationActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), "You can't calibrate the Muse headband if you're not connected to it!", Toast.LENGTH_SHORT).show();
            }*/
            // TODO: to remove later of course, this is just for the sake of testing
            startActivity(new Intent(ConnectionActivity.this, CalibrationActivity.class));

        }
        else if (v.getId() == R.id.start) {

            // The user has pressed the "Start" button.
            // Before starting the visualisation, the Muse headband must be both connected to the device and be calibrated.
            /*if (muse == null) {
                Toast.makeText(getApplicationContext(), R.string.toast_start_exception_not_co, Toast.LENGTH_SHORT).show();
            } else if (calibrated == false) {
                Toast.makeText(getApplicationContext(), R.string.toast_start_exception_not_ca, Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(ConnectionActivity.this, VisualActivity.class));
            }*/
            // TODO: to remove later of course, this is just for the sake of testing
            startActivity(new Intent(ConnectionActivity.this, VisualActivity.class));
        }
    }


    //--------------------------------------
    // Permissions

    /**
     * The ACCESS_COARSE_LOCATION permission is required to use the
     * Bluetooth Low Energy library and must be requested at runtime for Android 6.0+
     * On an Android 6.0 device, the following code will display 2 dialogs,
     * one to provide context and the second to request the permission.
     * On an Android device running an earlier version, nothing is displayed
     * as the permission is granted from the manifest.
     *
     * If the permission is not granted, then Muse 2016 (MU-02) headbands will
     * not be discovered and a SecurityException will be thrown.
     */
    private void ensurePermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // We don't have the ACCESS_COARSE_LOCATION permission so create the dialogs asking
            // the user to grant us the permission.
            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which){
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(ConnectionActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    0);
                        }
                    };

            // This is the context dialog which explains to the user the reason we are requesting
            // this permission.  When the user presses the positive (I Understand) button, the
            // standard Android permission dialog will be displayed (as defined in the button
            // listener above).
            AlertDialog introDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_description)
                    .setPositiveButton(R.string.permission_dialog_understand, buttonListener)
                    .setCancelable(false)
                    .create();
            introDialog.show();
        }
    }


    //--------------------------------------
    // Listeners

    // Listener class for scanning Muse headbands nearby
    private MuseListener museListener = new MuseListener() {

        /**
         * You will receive a callback to this method each time a headband is discovered.
         * In this example, we update the spinner with the MAC address of the headband.
         */
        @Override
        public void museListChanged() {
            final List<Muse> list = manager.getMuses();
            spinnerAdapter.clear();
            for (Muse m : list) {
                spinnerAdapter.add(m.getName() + " - " + m.getMacAddress());
            }
        }
    };

    // ConnectionListener will be notified whenever there is a change in the connection
    // state of a headband, for example when the headband connects or disconnects
    private MuseConnectionListener connectionListener = new MuseConnectionListener() {

        /**
         * You will receive a callback to this method each time there is a change to the
         * connection state of one of the headbands.
         * @param p     A packet containing the current and prior connection states
         * @param muse  The headband whose state changed.
         */
        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p, final Muse muse) {

            // Update the UI with the change in connection state.
            ConnectionState current = p.getCurrentConnectionState();
            status.setText(String.valueOf(current));
            Log.d(TAG1, "Status: " + current);

            if (current == ConnectionState.DISCONNECTED) {
                Log.d(TAG1, "Muse disconnected:" + muse.getName());
                // We have disconnected from the headband, so set our cached copy to null.
                ConnectionActivity.this.muse = null;
                spinnerAdapter.clear();
            }
        }
    };


    // DataListener is how you will receive EEG (and other) data from the headband

}
