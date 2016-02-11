package com.portfolio.course.esguti.popularmoviesapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout fragmentItemDetail = (FrameLayout) findViewById(R.id.container);
        if (fragmentItemDetail != null) { ; Log.d(LOG_TAG, "Is a Tablet"); }
        else{ Log.d(LOG_TAG, "Is a Phone");  }
    }

}
