package com.example.mindvisualizer;
package com.tabian.imageview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class VisualActivity extends AppCompatActivity {

    private static final String TAG = "VisualActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual);

        int color_change_variable = 0;

        SharedPreferences pref = getApplicationContext().getSharedPreferences("EegData", 0); // 0 - for private mode

        String avgEegAlphaString = pref.getString("avgEegAlphaString", "None");
        //Average alphawave:
        //This is the threshold value, which will be located in the "middle of the scale"
        int avgEegAlphaStringInt = Integer.parseInt(avgEegAlphaString);

        TextView picture1 = findViewById(R.id.picture1);
        if(color_change_variable <= 10) {
            //view.setBackgroundResource(R.color.lightBlue);
        }
        else if(color_change_variable <= 20){
            //view.setBackgroundResource(R.color.deepPurple);
        }
        else if(color_change_variable <= avgEegAlphaStringInt){
            //view.setBackgroundResource(R.color.Purple);
        }
        else if(color_change_variable <= 40){
            //view.setBackgroundResource(R.color.Pink);
        }
        else if(color_change_variable <= 50){
            //view.setBackgroundResource(R.color.Red);
        }
    }
}
