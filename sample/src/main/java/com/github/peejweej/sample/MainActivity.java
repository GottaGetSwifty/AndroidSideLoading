package com.github.peejweej.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void loadClicked(View view) {
        startActivity(SharingHelper.getIntentForLoading(getApplicationContext()));
    }

    public void shareClicked(View view) {
        startActivity(SharingHelper.getIntentForSharing(getApplicationContext()));
    }

    public void shareFragmentClicked(View view) {
    }

    public void loadFragmentClicked(View view) {
    }
}