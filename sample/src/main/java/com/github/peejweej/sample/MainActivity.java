package com.github.peejweej.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.peejweej.androidsideloading.fragments.SideLoadTypeChoosingFragment;
import com.github.peejweej.androidsideloading.fragments.TypeChoosingFragment;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void loadClicked(View view) {
        startActivity(SharingHelper.getIntentForLoading(getApplicationContext()));
    }

    public void loadFragmentClicked(View view) {

        SideLoadTypeChoosingFragment.constructFragment(SharingHelper.getLoadInformation(), new SideLoadTypeChoosingFragment.SideLoadChoosingFragmentListener() {
            @Override
            public void finishWithFile(Uri file) {

                Log.i(TAG, file.toString());
            }

            @Override
            public void finishWithText(String text) {
                Log.i(TAG, text);
            }
        }).show(getSupportFragmentManager(), "TypeChoosingFragment");
    }

    public void shareClicked(View view) {
        startActivity(SharingHelper.getIntentForSharing(getApplicationContext()));
    }

    public void shareFragmentClicked(View view) {

        TypeChoosingFragment.constructFragment(SharingHelper.getShareInformation(getApplicationContext()))
                .show(getSupportFragmentManager(), "TypeChoosingFragment");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = SideLoadTypeChoosingFragment.getUriFromActivityResult(requestCode, resultCode, data, SharingHelper.getLoadInformation());
        Log.i(TAG, uri.toString());
    }
}