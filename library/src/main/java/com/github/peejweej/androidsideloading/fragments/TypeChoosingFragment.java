package com.github.peejweej.androidsideloading.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;

import com.github.peejweej.androidsideloading.R;
import com.github.peejweej.androidsideloading.adapters.ShareAdapter;
import com.github.peejweej.androidsideloading.model.SideLoadInformation;
import com.github.peejweej.androidsideloading.model.SideLoadType;
import com.github.peejweej.androidsideloading.utilities.LoadManager;
import com.github.peejweej.androidsideloading.utilities.ShareManager;


public class TypeChoosingFragment extends DialogFragment {

    private static final String INFO_PARAM = "INFO";

    protected GridView gridView;
    protected ShareAdapter adapter;

    protected SideLoadInformation info;
    protected TypeChosenListener typeChosenListener;

    public static TypeChoosingFragment constructFragment(SideLoadInformation info){

        TypeChoosingFragment fragment = new TypeChoosingFragment();

        if(info != null) {
            Bundle extras = new Bundle();
            extras.putSerializable(INFO_PARAM, info);
            fragment.setArguments(extras);
        }
        return fragment;
    }

    public TypeChoosingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            this.info = (SideLoadInformation) getArguments().getSerializable(INFO_PARAM);
        }
        setupTypeChosenListener();
    }

    protected void setupTypeChosenListener(){
        typeChosenListener = new ShareManager((AppCompatActivity) getActivity(), info, new ShareManager.ShareManagerListener() {
            @Override
            public void finished() {
                dismiss();
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_side_load_type_choosing, container, false);
        setupViews(view);
        return view;
    }

    private void setupViews(View view){

        gridView = (GridView) view.findViewById(R.id.side_load_list_view);
        boolean isSharing = (info == null || info.fileName != null);
        boolean canUseQrCode = (isSharing && info != null && info.file != null && info.file.length() < 1024);
        adapter = new ShareAdapter(getActivity(),
                SideLoadType.getListOfSideLoadTypes(getActivity().getApplicationContext(), !isSharing, canUseQrCode));
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedType(adapter.getItem(position));
            }
        });
    }

    protected void selectedType(SideLoadType type){
        typeChosenListener.typeWasChosen(type);
    }

    public interface TypeChosenListener {
        void typeWasChosen(SideLoadType type);
    }

    static public Uri getUriFromActivityResult(int requestCode, int resultCode, Intent data, SideLoadInformation information) {
        return LoadManager.getUriFromActivityResult(requestCode, resultCode, data, information);
    }
}
