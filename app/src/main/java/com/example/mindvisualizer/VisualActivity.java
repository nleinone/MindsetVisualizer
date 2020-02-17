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
import java.util.stream.Collectors;

public class VisualActivity extends AppCompatActivity {

    private static final String TAG = "VisualActivity";


    public void loadDataFromFirebase(String sessionId)
    {
        Log.v("test","load 1");
        //Get firebase database reference:
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference firebaseRootReference = database.getReference("musefirebase-79096");
        DatabaseReference sessionReference = firebaseRootReference.child("Session-" + sessionId);
        Log.v("test","load 2");

        sessionReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        ArrayList<String> dataList = new ArrayList<>();
                        // Result will be holded Here
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            String strValue = String.valueOf(dsp.getValue());
                            Log.v("test", "eegValuetest: " + dsp.getValue());
                            Log.v("test", "eegValueStr: " + strValue);

                            dataList.add(String.valueOf(dsp.getValue())); //add result into array list

                        }

                        int len = dataList.size();
                        Log.v("test", "lenDataList func: " + len);
                        Log.v("test","load 3");

                        SharedPreferences prefsAvg = getApplicationContext().getSharedPreferences("prefsAvg", 0);
                        float avgEEG = getAvg(dataList);
                        Log.v("test", "avgEEG: " + String.valueOf(avgEEG));
                        prefsAvg.edit().putString("avgEEG", String.valueOf(avgEEG)).apply();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                        Log.v("test", "Error in handling Firebase data");
                    }
                });

    }


    private float getAvg(ArrayList<String> dataList)
    {
        float sum = 0;
        float floatNum = 0;

        int len = dataList.size();
        Log.v("getAvg", "lenDataList func: " + len);

        for (String i : dataList) {
            floatNum = Float.parseFloat(i);
            Log.v("getAvg", "floatNum: " + floatNum);
            sum = sum + floatNum;
            Log.v("getAvg", "avgEEG: " + sum);
            String stringEEG = String.valueOf(floatNum);
            Log.v("getAvg", "stringEEG: " + stringEEG);
        }
        Log.v("getAvg", "avgEEGReturn: " + sum);
        float avgEEG = sum / len;
        return avgEEG;
    }

    public void runColorChange(Float color_change_variable)
    {
        //get current sessionId:
        SharedPreferences prefCalibrationMode = getApplicationContext().getSharedPreferences("prefCalibrationMode", 0); // 0 - for private mode
        String calibMode = prefCalibrationMode.getString("CalibrationMode", "0");

        SharedPreferences prefsAvg = getApplicationContext().getSharedPreferences("prefsAvg", 0);
        String avgEEG = prefsAvg.getString("avgEEG", "");
        Log.v("VisualActivity", "avgEEG2: " + String.valueOf(avgEEG));
        float avgEEGFloat = Float.parseFloat(avgEEG);
        Log.v("VisualActivity", "Load function result: " + avgEEGFloat);

        if(calibMode.equals("2"))
        {

            String avgEEGString = String.valueOf(avgEEG);
            Log.v("VisualActivity", "avgEEGString: " + avgEEGString);
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

            //https://htmlcolorcodes.com/
            //https://www.betterhelp.com/advice/general/what-are-alpha-brain-waves/

            int direction = -1;

            if(color_change_variable >= avgEEGFloat) {

                RL.setBackgroundColor(Color.parseColor("#69FF40"));
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.10) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#8FFF40"));
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.15) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#A9FF40") );
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.20) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#C6FF40"));
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.25) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#E3FF40"));
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.30) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#FFFC40"));
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.35) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#FFDC40"));
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.40) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#FFBF40"));
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.45) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#FF9940"));
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.50) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#FF7440"));
            }
            else if(color_change_variable >= avgEEGFloat + (avgEEGFloat * 0.55) * (direction)){
                RL.setBackgroundColor(Color.parseColor("#FF5D40"));
            }
            else
            {
                RL.setBackgroundColor(Color.parseColor("#FF4040"));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual);
        //Calculate threshold value:
        SharedPreferences prefCalibrationMode = getApplicationContext().getSharedPreferences("prefCalibrationMode", 0); // 0 - for private mode
        String sessionId = prefCalibrationMode.getString("SessionId", "0");
        String calibMode = prefCalibrationMode.getString("CalibrationMode", "0");

        if(calibMode.equals("2"))
        {
            loadDataFromFirebase(sessionId);
        }

        SharedPreferences prefEeg = getApplicationContext().getSharedPreferences("avgEEG", 0);
        prefEeg.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        finish();
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        finish();
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
