package com.example.mindvisualizer;
//package com.tabian.imageview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class VisualActivity extends AppCompatActivity {

    private static final String TAG = "VisualActivity";

    public ArrayList<String> loadDataFromFirebase(String sessionId)
    {
        Log.v("VisualActivity","load 1");
        //Get firebase database reference:
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference firebaseRootReference = database.getReference("musefirebase-79096");
        DatabaseReference sessionReference = firebaseRootReference.child("Session-" + sessionId);

        Log.v("VisualActivity","load 2");

        ArrayList<String> dataList = new ArrayList<>();
        sessionReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        ArrayList<String> dataList = new ArrayList<>();
                        // Result will be holded Here
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            Log.v("VisualActivity", "eegValue: " + dsp.getValue());
                            dataList.add(String.valueOf(dsp.getValue())); //add result into array list

                        }
                        Log.v("VisualActivity","load 3");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                        Log.v("VisualActivity", "Error in handling Firebase data");

                    }
                });
        Log.v("VisualActivity","load 4");
        return dataList;
    }

    private float getAvg(ArrayList<String> dataList)
    {
        float avgEEG = 0;
        float floatNum = 0;
        for (String i : dataList)
            floatNum = Float.parseFloat(i);
            avgEEG = avgEEG + floatNum;

        return avgEEG;
    }

    public void runColorChange(Float color_change_variable)
    {
        //get current sessionId:
        SharedPreferences prefCalibrationMode = getApplicationContext().getSharedPreferences("prefCalibrationMode", 0); // 0 - for private mode
        String sessionId = prefCalibrationMode.getString("SessionId", "0");
        String calibMode = prefCalibrationMode.getString("CalibrationMode", "0");

        if(calibMode.equals("2"))
        {
            ArrayList<String> dataList = new ArrayList<>();
            dataList = loadDataFromFirebase(sessionId);
            float avgEEG = getAvg(dataList);
            String avgEEGString = String.valueOf(avgEEG);
            SharedPreferences prefEeg = getApplicationContext().getSharedPreferences("avgEEG", 0);
            prefEeg.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

            TextView displayCurrent = findViewById(R.id.displayCurrentEEG);
            TextView displayThreshold = findViewById(R.id.displayThreshold);
            displayCurrent.setText(color_change_variable.toString());
            displayThreshold.setText(avgEEGString);
            //Average alphawave:
            //This is the threshold value, which will be located in the "middle of the scale"
            RelativeLayout RL;
            RL = (RelativeLayout)findViewById(R.id.Layout);


            if(color_change_variable <= avgEEG) {

                RL.setBackgroundColor(Color.parseColor("#99CCFF"));
            }
            else if(color_change_variable <= avgEEG + 0.05){
                RL.setBackgroundColor(Color.parseColor("#6666FF"));
            }
            else if(color_change_variable <= avgEEG + 0.10){
                RL.setBackgroundColor(Color.parseColor("#9933FF"));
            }
            else if(color_change_variable <= avgEEG + 0.15){
                RL.setBackgroundColor(Color.parseColor("#FF00FF"));
            }
            else if(color_change_variable <= avgEEG + 0.20){
                RL.setBackgroundColor(Color.parseColor("#CC0066"));
            }
            else
            {
                RL.setBackgroundColor(Color.parseColor("#FF0000"));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual);
        SharedPreferences prefEeg = getApplicationContext().getSharedPreferences("avgEEG", 0);
        prefEeg.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

    }
    //Do stuff when key is changed
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("averageEEG")){
                Float value = sharedPreferences.getFloat("averageEEG", 0);
                runColorChange(value);
            }
        }
    };
}
