package com.github.peejweej.androidsideloading.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
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


public class SideLoadTypeChoosingFragment extends Fragment {

    private static final String INFO_PARAM = "INFO";
    private static final String IS_SHARING_PARAM = "IS_SHARING_PARAM";

    private GridView gridView;
    private ShareAdapter adapter;

    private boolean isSharing;
    private SideLoadInformation info;
    private TypeChosenListener typeChosenListener;
    private ChoosingFragmentListener fragmentListener;

    public static SideLoadTypeChoosingFragment constructFragment(SideLoadInformation info, boolean isSharing){

        SideLoadTypeChoosingFragment fragment = new SideLoadTypeChoosingFragment();

        if(info != null) {
            Bundle extras = new Bundle();
            extras.putSerializable(INFO_PARAM, info);
            extras.putBoolean(IS_SHARING_PARAM, isSharing);
            fragment.setArguments(extras);
        }
        return fragment;
    }

    public SideLoadTypeChoosingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            this.fragmentListener = (ChoosingFragmentListener) getActivity();
            this.info = (SideLoadInformation) getArguments().getSerializable(INFO_PARAM);
            isSharing = getArguments().getBoolean(IS_SHARING_PARAM);
        }

        if(isSharing) {
            typeChosenListener = new ShareManager((ActionBarActivity) getActivity(), info);
        }
        else{
            typeChosenListener = new LoadManager((ActionBarActivity) getActivity(), info, new LoadManager.LoadManagerListener() {
                @Override
                public void finishWithFile(Uri fileUri) {
                    fragmentListener.finishWithFile(fileUri);
                }

                @Override
                public void finishWithText(String text) {
                    fragmentListener.finishWithText(text);
                }
            });
        }
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

    private void selectedType(SideLoadType type){
        typeChosenListener.typeWasChosen(type);
    }

    public interface TypeChosenListener {
        void typeWasChosen(SideLoadType type);
    }

    public interface ChoosingFragmentListener{
        void finishWithFile(Uri file);
        void finishWithText(String text);
    }

    static public Uri getUriFromActivityResult(int requestCode, int resultCode, Intent data, SideLoadInformation information) {
        return LoadManager.getUriFromActivityResult(requestCode, resultCode, data, information);
    }
}
