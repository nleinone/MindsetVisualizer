package com.example.mindvisualizer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;





public class VisualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual);

        int color_change_variable = 0;

        //Update eegValue, see reference at EegValueUpdater

        TextView picture1 = findViewById(R.id.picture1);

        if(color_change_variable == 0)
        {
            //Do something

            picture1.setText("This is now picture 1");
            //pictureObject picture1 = pictureObject1
        }

        else if(color_change_variable == 1)
        {
            //Do something
            picture1.setText("This is now picture 2");


            //pictureObject picture2 = pictureObject2
        }


    }
}
