package com.github.peejweej.androidsideloading.utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import com.github.peejweej.androidsideloading.R;
import com.github.peejweej.androidsideloading.activities.AnimationParadigm;
import com.github.peejweej.androidsideloading.activities.FileFinderActivity;
import com.github.peejweej.androidsideloading.file.FileChooserDialog;
import com.github.peejweej.androidsideloading.fragments.LoadingFragment;
import com.github.peejweej.androidsideloading.fragments.SideLoadTypeChoosingFragment;
import com.github.peejweej.androidsideloading.model.SideLoadInformation;
import com.github.peejweej.androidsideloading.model.SideLoadType;
import com.github.peejweej.androidsideloading.wifiDirect.WiFiDirectActivity;

import java.io.File;

/**
 * Created by Fechner on 11/17/15.
 */
public class LoadManager implements SideLoadTypeChoosingFragment.TypeChosenListener {

    private LoadingFragment loadingFragment;
    private ActionBarActivity activity;
    private SideLoadInformation sideLoadInformation;

    private LoadManagerListener listener;

    public LoadManager(ActionBarActivity activity, SideLoadInformation sideLoadInformation, LoadManagerListener listener) {
        this.activity = activity;
        this.sideLoadInformation = sideLoadInformation;
        this.listener = listener;
    }

    private Context getApplicationContext(){
        return activity.getApplicationContext();
    }

    public void typeWasChosen(SideLoadType type) {

        switch (type){
            case SIDE_LOAD_TYPE_WIFI:
                startWIFILoadAction();
                break;
            case SIDE_LOAD_TYPE_FILE:
            case SIDE_LOAD_TYPE_STORAGE:
                startStorageLoadAction();
                break;
            case SIDE_LOAD_TYPE_SD_CARD:
                startSDCardLoadAction();
                break;
            case SIDE_LOAD_TYPE_AUTO_FIND:
                startAutoFindAction();
                break;
            default:
                break;
        }
    }

    private void startWIFILoadAction(){

        Intent intent = new Intent(getApplicationContext(), WiFiDirectActivity.class);
        activity.startActivity(intent);
    }

    private void startAutoFindAction(){
        int enterAnimation = AnimationParadigm.getNextAnimationEnter(AnimationParadigm.ANIMATION_VERTICAL);
        int closeAnimation = AnimationParadigm.getNextAnimationExit(AnimationParadigm.ANIMATION_VERTICAL);

        activity.startActivityForResult(new Intent(getApplicationContext(), FileFinderActivity.class)
                .putExtra(FileFinderActivity.LOAD_INFO_PARAM, sideLoadInformation), 0);
        activity.overridePendingTransition(enterAnimation, closeAnimation);
    }

    private void startStorageLoadAction(){
        loadStorage(null);
    }

    private void startSDCardLoadAction(){
        loadStorage("/" + getApplicationContext().getString(R.string.library_name));
    }

    private void loadStorage(String optionalDir){

        String finalDir = Environment.getExternalStorageDirectory().getPath();
        if(optionalDir != null && new File(finalDir + optionalDir).exists()){
            finalDir += optionalDir;
        }
        FileChooserDialog dialog = new FileChooserDialog(activity, finalDir);
        dialog.setFilter(".*" + sideLoadInformation.fileExtension);
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {

            public void onFileSelected(Dialog source, File file) {
                listener.finishWithFile(Uri.fromFile(file));
                source.dismiss();
            }

            public void onFileSelected(Dialog source, File folder, String name) {
                listener.finishWithFile(Uri.fromFile(new File(folder.getAbsolutePath() + name)));
                source.dismiss();
            }
        });
        dialog.show();
    }

//    private Uri finishWithFile(Uri fileUri){
//
//        if(sideLoadInformation.fileVerifier != null && sideLoadInformation.fileVerifier.fileIsValid(fileUri)){
//            setResult(0, new Intent(getApplicationContext(), SideLoadActivity.class).setData(fileUri));
//            handleBack();
//        }
//        else {
//            showFailureAlert();
//        }
//    }
//
//    private void finishWithText(String text){
//
//        if(sideLoadInformation.fileVerifier != null && sideLoadInformation.fileVerifier.fileIsValid(text)){
//            showFailureAlert();
//        }
//        else {
//            setResult(0, new Intent(getApplicationContext(), SideLoadActivity.class).putExtra(FILE_TEXT_PARAM, text));
//            handleBack();
//        }
//    }

    private void showFailureAlert(){

        setLoadingFragmentVisibility(false, "", false);
        View titleView = View.inflate(getApplicationContext(), R.layout.alert_title, null);
        ((TextView) titleView.findViewById(R.id.alert_title_text_view)).setText("Load Failure");
        new AlertDialog.Builder(activity)
                .setCustomTitle(titleView)
                .setMessage("Error loading file. Please try again")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        onBackPressed(true);
                    }
                })
                .show();
    }

    /**
     * Creates and Loading fragment.
     * @param visible true if the fragment should be visible
     * @param loadingText text to show in the loading fragment
     * @param cancelable whether the fragment should be cancelable.
     */
    public void setLoadingFragmentVisibility(final boolean visible, final String loadingText, final boolean cancelable){

        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (!visible) {
                    if (loadingFragment != null) {
                        loadingFragment.dismiss();
                    }
                } else {
                    if (loadingFragment == null) {
                        loadingFragment = LoadingFragment.newInstance(loadingText);

                        loadingFragment.setCancelable(cancelable);
                        loadingFragment.setListener(new LoadingFragment.LoadingFragmentInteractionListener() {
                            public void loadingCanceled() {
                                loadingFragment.dismiss();
                                activity.getSupportFragmentManager().popBackStackImmediate();
                                loadingFragment = null;
                            }
                        });

                        loadingFragment.show(activity.getSupportFragmentManager(), LoadingFragment.TAG);

                    } else if (!loadingFragment.isVisible()) {
                        loadingFragment.show(activity.getSupportFragmentManager(), LoadingFragment.TAG);
                    }
                    loadingFragment.setLoadingText(loadingText);
                    loadingFragment.setCanCancel(cancelable);
                }
            }
        });
    }

    public interface LoadManagerListener {
        void finishWithFile(Uri fileUri);
        void finishWithText(String text);
    }

    static public Uri getUriFromActivityResult(int requestCode, int resultCode, Intent data, SideLoadInformation information) {

        if(requestCode == resultCode && data != null && data.getData() != null){
            Uri fileUri = data.getData();
            if(information.fileVerifier != null && information.fileVerifier.fileIsValid(fileUri)) {
                return fileUri;
            }
        }
        return null;
    }
}
