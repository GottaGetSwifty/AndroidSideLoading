package com.github.peejweej.androidsideloading.utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.peejweej.androidsideloading.R;
import com.github.peejweej.androidsideloading.file.FileChooserDialog;
import com.github.peejweej.androidsideloading.fragments.LoadingFragment;
import com.github.peejweej.androidsideloading.fragments.TypeChoosingFragment;
import com.github.peejweej.androidsideloading.model.SideLoadInformation;
import com.github.peejweej.androidsideloading.model.SideLoadType;
import com.github.peejweej.androidsideloading.wifiDirect.WiFiDirectActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by Fechner on 11/17/15.
 */
public class ShareManager implements TypeChoosingFragment.TypeChosenListener {

    private LoadingFragment loadingFragment;
    private AppCompatActivity activity;
    private SideLoadInformation sideLoadInformation;
    private ShareManagerListener listener;

    public ShareManager(AppCompatActivity activity, SideLoadInformation sideLoadInformation, ShareManagerListener listener) {
        this.listener = listener;
        this.activity = activity;
        this.sideLoadInformation = sideLoadInformation;
    }

    private Context getApplicationContext(){
        return activity.getApplicationContext();
    }

    public void typeWasChosen(SideLoadType type) {

        switch (type){
            case SIDE_LOAD_TYPE_BLUETOOTH:
                startBluetoothShareAction();
                break;
            case SIDE_LOAD_TYPE_NFC:
                break;
            case SIDE_LOAD_TYPE_WIFI:
                startWIFIShareAction();
                break;
            case SIDE_LOAD_TYPE_STORAGE:
                startStorageShareAction();
                break;
            case SIDE_LOAD_TYPE_QR_CODE:
                break;
            case SIDE_LOAD_TYPE_SD_CARD:
                startSDCardShareAction();
                break;
            case SIDE_LOAD_TYPE_AUTO_FIND:
                break;
            case SIDE_LOAD_TYPE_FILE:
                break;
            case SIDE_LOAD_TYPE_OTHER:
                startShareOtherAction();
                break;
            default:
                break;
        }
    }

    private Uri getFileUri(){

        if(sideLoadInformation.getUri() != null){
            return sideLoadInformation.getUri();
        }
        else{
            try {
                return FileUtilities.createTemporaryFile(getApplicationContext(), sideLoadInformation.file.getBytes("UTF-8"), sideLoadInformation.fileName);
            }
            catch (UnsupportedEncodingException e){
                e.printStackTrace();
                return null;
            }
        }
    }

    private void startShareOtherAction(){

        Uri fileUri = getFileUri();

        Intent sharingIntent = new Intent(
                Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        activity.startActivity(sharingIntent);
        listener.finished();
    }

    private void startBluetoothShareAction(){

        View titleView = View.inflate(getApplicationContext(), R.layout.alert_title, null);
        ((TextView) titleView.findViewById(R.id.alert_title_text_view)).setText("Start Bluetooth Sharing");

        AlertDialog dialogue = new AlertDialog.Builder(activity)
                .setCustomTitle(titleView)
                .setMessage(getApplicationContext().getString(R.string.bluetooth_turn_on_message))
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openBluetoothSharing();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        dialogue.show();
        listener.finished();
    }

    private void openBluetoothSharing(){

        Uri fileUri = getFileUri();

        Intent sharingIntent = new Intent(
                Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent
                .setComponent(new ComponentName(
                        "com.android.bluetooth",
                        "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        activity.startActivity(sharingIntent);
        listener.finished();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        View titleView = View.inflate(getApplicationContext(), R.layout.alert_title, null);
//        ((TextView) titleView.findViewById(R.id.alert_title_text_view)).setText("Bluetooth Sharing");
//
//        if(resultCode == 0){
//            AlertDialog dialogue = new AlertDialog.Builder(this)
//                    .setCustomTitle(titleView)
//                    .setMessage(getApplicationContext().getString(R.string.bluetooth_directions_text))
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            handleBack();
//                        }
//                    }).create();
//            dialogue.show();
//        }
//    }

    private void startNFCShareAction(){

        Uri fileUri = getFileUri();

        Intent sharingIntent = new Intent(
                Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
//        sharingIntent
//                .setComponent(new ComponentName(
//                        "com.android.nfchip" +
//                                "",
//                        "com.android.nfc.opp.BeamShareActivity"));
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        activity.startActivity(sharingIntent);
        listener.finished();
    }

    private void startWIFIShareAction(){

        Uri fileUri = getFileUri();

        Intent intent = new Intent(getApplicationContext(), WiFiDirectActivity.class)
                .setData(fileUri);
        activity.startActivity(intent);
        listener.finished();
    }

    private void startStorageShareAction(){

        final FileChooserDialog dialog = new FileChooserDialog(activity, Environment.getExternalStorageDirectory().getAbsolutePath());
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                saveToFile(file);
                dialog.dismiss();
                showSuccessAlert(true, file.getAbsolutePath());
            }

            public void onFileSelected(Dialog source, File folder, String name) {
                saveToFile(new File(folder.getAbsolutePath() + "/" + name));
                dialog.dismiss();
                showSuccessAlert(true, folder.getAbsolutePath());
            }
        });
        dialog.setFolderMode(true);
        dialog.setShowConfirmation(true, false);
        dialog.show();
        listener.finished();
    }

    private void saveToFile(File folder){

        unzipFiles(new File(sideLoadInformation.getUri().getPath()), folder);
        FileUtilities.saveFile(getFileBytes(), folder.getPath(), sideLoadInformation.fileName);
    }

    private void unzipFiles(File archive, File newFile){
        try {
            ZipFile zipfile = new ZipFile(archive);
            int entries = zipfile.size();
            int total = 0;

            for (Enumeration<?> e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, newFile);
            }
            zipfile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry,
                            File outputDir) throws IOException {

//        if (entry.isDirectory()) {
//            outputDir.mkdirs();
//            createDir(new File(outputDir, entry.getName()));
//            return;
//        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        BufferedInputStream inputStream = new
                BufferedInputStream(zipfile
                .getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(outputFile));

        try{
            copy(inputStream, outputStream);
        }
        finally{
            outputStream.close();
            inputStream.close();
        }
    }

    public static int copy(BufferedInputStream in, BufferedOutputStream out) {
        byte[] buffer = new byte[1024];

        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, 1024)) != (-1)) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    private byte[] getFileBytes(){

        if(sideLoadInformation.file != null){

            try {
                return sideLoadInformation.file.getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException e){
                e.printStackTrace();
                return null;
            }
        }
        else{
            return FileUtilities.getBytesFromFile(new File(sideLoadInformation.getUri().getPath()));
        }
    }

    private void startSDCardShareAction(){

        setLoadingFragmentVisibility(true, "Saving", false);
        FileUtilities.saveFileToSdCard(getApplicationContext(), getFileBytes(), sideLoadInformation.fileName);

        String fileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                + getApplicationContext().getString(R.string.library_name);
        showSuccessAlert(true, fileDir);
        listener.finished();
    }

    private void showSuccessAlert(boolean success, String filePath){

        setLoadingFragmentVisibility(false, "", false);
        View titleView = View.inflate(getApplicationContext(), R.layout.alert_title, null);
        ((TextView) titleView.findViewById(R.id.alert_title_text_view)).setText("Share Status");
        new AlertDialog.Builder(activity)
                .setCustomTitle(titleView)
                .setMessage((success)? "Sharing was successful to directory:\n" + filePath : "Sharing failed")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listener.finished();
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

    public interface ShareManagerListener{
        void finished();
    }
}
