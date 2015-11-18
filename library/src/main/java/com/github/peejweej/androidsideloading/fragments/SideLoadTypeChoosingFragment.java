package com.github.peejweej.androidsideloading.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.github.peejweej.androidsideloading.R;
import com.github.peejweej.androidsideloading.adapters.ShareAdapter;
import com.github.peejweej.androidsideloading.model.SideLoadInformation;
import com.github.peejweej.androidsideloading.model.SideLoadType;
import com.github.peejweej.androidsideloading.utilities.LoadManager;
import com.github.peejweej.androidsideloading.utilities.ShareManager;


public class SideLoadTypeChoosingFragment extends TypeChoosingFragment {

    private static final String INFO_PARAM = "INFO";

    private SideLoadChoosingFragmentListener fragmentListener;

    public static SideLoadTypeChoosingFragment constructFragment(SideLoadInformation info, SideLoadChoosingFragmentListener listener){

        SideLoadTypeChoosingFragment fragment = new SideLoadTypeChoosingFragment();

        if(info != null) {
            Bundle extras = new Bundle();
            extras.putSerializable(INFO_PARAM, info);
            fragment.setArguments(extras);
        }
        fragment.fragmentListener = listener;
        return fragment;
    }

    public SideLoadTypeChoosingFragment() {
    }

    @Override
    protected void setupTypeChosenListener() {
        this.typeChosenListener = new LoadManager((AppCompatActivity) getActivity(), info, new LoadManager.LoadManagerListener() {
            @Override
            public void finishWithFile(Uri fileUri) {
                fragmentListener.finishWithFile(fileUri);
            }

            @Override
            public void finishWithText(String text) {
                fragmentListener.finishWithText(text);
            }

            @Override
            public void finished() {
                dismiss();
            }
        });
    }

    @Override
    protected void selectedType(SideLoadType type){
        typeChosenListener.typeWasChosen(type);
    }

    static public Uri getUriFromActivityResult(int requestCode, int resultCode, Intent data, SideLoadInformation information) {
        return LoadManager.getUriFromActivityResult(requestCode, resultCode, data, information);
    }

    public interface SideLoadChoosingFragmentListener{
        void finishWithFile(Uri file);
        void finishWithText(String text);
    }
}
