package com.github.peejweej.androidsideloading.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.github.peejweej.androidsideloading.R;
import com.github.peejweej.androidsideloading.fragments.LoadingFragment;
import com.github.peejweej.androidsideloading.view.ToolbarViewGroup;


/**
 * A login screen that offers login via email/password.
 */
abstract public class BaseActivity extends ActionBarActivity implements ToolbarViewGroup.UWToolbarListener{

    private static final String TAG = "UWBaseActivity";

    private ToolbarViewGroup toolbar;
    private LoadingFragment loadingFragment;
    private RelativeLayout loadingBox;

    protected boolean isLoading;
    private boolean isActive = false;

    /**
     *
     * @return The AnimationParadigm for this activity
     */
    abstract public AnimationParadigm getAnimationParadigm();

    //region Parent Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isActive = true;

    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        isActive = true;
    }
    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
        setupToolbar(false);
    }
    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBack();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        handleBack();
    }

    /**
     *Before 2.0
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //endregion

    //region Accessors

    public boolean isActive() {
        return isActive;
    }

    public ToolbarViewGroup getToolbar(){

        return toolbar;
    }

    //endregion

    //region Setup

    /**
     * Sets up the toolbar with the passed parameters
     * @param hasLogo will show the logo if true
     * @param titleText will show the title with this text
     * @param titleClickable whether the title should be clickable
     * @param rightButtonClickable whether the right button should be shown and clickable.
     */
    public void setupToolbar(boolean hasLogo, String titleText, boolean titleClickable, boolean rightButtonClickable){

        setupToolbar(hasLogo, titleText, titleClickable);
//        toolbar.setRightImageVisible(rightButtonClickable);
    }

    /**
     * Sets up the toolbar with the passed parameters
     * @param hasLogo will show the logo if true
     * @param titleText will show the title with this text
     * @param titleClickable whether the title should be clickable
     */
    public void setupToolbar(boolean hasLogo, String titleText, boolean titleClickable){

        setupToolbar(hasLogo);
        toolbar.setTitle(titleText, titleClickable);
    }

    /**
     * Sets up the toolbar based on the passed parameters
     * @param hasLogo will show the logo if true
     */
    public void setupToolbar(boolean hasLogo){

        toolbar = new ToolbarViewGroup((Toolbar) findViewById(R.id.toolbar), this, hasLogo, getBackResource(), this);
        setToolbarColor(getResources().getColor(R.color.primary_dark));
    }

    /**
     * Will set the background color of the toolbar
     * @param color the desired color resource of the toolbar
     */
    protected void setToolbarColor(int color){
        this.toolbar.setBackgroundColor(color);
    }

    /**
     *
     * @return resource for the back button
     */
    public int getBackResource(){
        if(getAnimationParadigm() == AnimationParadigm.ANIMATION_VERTICAL ||
                getAnimationParadigm() == AnimationParadigm.ANIMATION_FORWARD_RIGHT_BACK_DOWN){
            return R.drawable.x_button;
        }
        else {
            return R.drawable.back_button_light;
        }
    }

    //endregion

    //region user interaction

    //endregion


    /**
     * Creates and Loading fragment.
     * @param visible true if the fragment should be visible
     * @param loadingText text to show in the loading fragment
     * @param cancelable whether the fragment should be cancelable.
     */
    public void setLoadingFragmentVisibility(final boolean visible, final String loadingText, final boolean cancelable){

        runOnUiThread(new Runnable() {
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
                                getSupportFragmentManager().popBackStackImmediate();
                                loadingFragment = null;
                            }
                        });

                        loadingFragment.show(getSupportFragmentManager(), LoadingFragment.TAG);

                    } else if (!loadingFragment.isVisible()) {
                        loadingFragment.show(getSupportFragmentManager(), LoadingFragment.TAG);
                    }
                    loadingFragment.setLoadingText(loadingText);
                    loadingFragment.setCanCancel(cancelable);
                }
            }
        });
    }

    /**
     * rotates the screen to portrait if it's currently in landscape, or landscape it it's currently in portrait
     * @param shouldMakeSensorBased whether the activity should reset to sensor based orientation after changing
     */
    protected void rotate(boolean shouldMakeSensorBased){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if(shouldMakeSensorBased){
            waitAndMakeOrientationSensorBased();
        }
    }

    /**
     * waits for 2 second and makes the activity's orientation sensor based
     */
    protected void waitAndMakeOrientationSensorBased(){

        Thread thread = new Thread("orientationThread") {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(2000);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        };
        thread.start();
    }

    //endregion

    //region Changing Activities

    /**
     * Starts new activity with a vertical popover-like animation
     * @param nextClass the class of the new activity
     */
    public void goToNewActivity(Class nextClass){
        goToNewActivity(new Intent(getApplicationContext(), nextClass));
    }

    /**
     * Starts new activity with a vertical popover-like animation
     * @param intent Intent of the new activity
     */
    public void goToNewActivity(Intent intent){

        int enterAnimation = AnimationParadigm.getNextAnimationEnter(AnimationParadigm.ANIMATION_VERTICAL);
        int exitAnimation = AnimationParadigm.getNextAnimationExit(AnimationParadigm.ANIMATION_VERTICAL);
        startActivity(intent);
        overridePendingTransition(enterAnimation, exitAnimation);
    }

    /**
     * starts a new activity with the activity's forward animation
     * @param nextClass class for new activity
     */
    public void goToNextActivity(Class nextClass){

        goToNextActivity(new Intent(getApplicationContext(), nextClass));
    }

    /**
     * starts a new activity with the activity's forward animation
     * @param intent Intent for new activity
     */
    public void goToNextActivity(Intent intent){

        int enterAnimation = AnimationParadigm.getNextAnimationEnter(getAnimationParadigm());
        int exitAnimation = AnimationParadigm.getNextAnimationExit(getAnimationParadigm());

        startActivity(intent);
        overridePendingTransition(enterAnimation, exitAnimation);
    }

    public void onBackPressed(boolean isSharing) {
        handleBack();
    }

    /**
     * goes back to the previous activity using the activity's back animation
     */
    protected void handleBack(){

        int enterAnimation = AnimationParadigm.getEndingAnimationEnter(getAnimationParadigm());
        int exitAnimation = AnimationParadigm.getEndingAnimationExit(getAnimationParadigm());
        finish();
        overridePendingTransition(enterAnimation, exitAnimation);
    }

    /**
     * goes to a new activity using the activity's back animation
     * @param activity new activity class to start
     */
    protected void goBackToActivity(Class activity){

        goBackToActivity(activity, null);
    }

    /**
     * starts a new activity using the activity's back animation
     * @param activity new activity class to start
     * @param extras option extras to add to the new Intent
     */
    protected void goBackToActivity(Class activity, Bundle extras){

        int enterAnimation = AnimationParadigm.getEndingAnimationEnter(AnimationParadigm.ANIMATION_VERTICAL);
        int exitAnimation = AnimationParadigm.getEndingAnimationExit(AnimationParadigm.ANIMATION_VERTICAL);

        Intent intent = new Intent(getApplicationContext(), activity);
        if(extras != null){
            intent.putExtras(extras);
        }

        startActivity(intent);
        overridePendingTransition(enterAnimation, exitAnimation);
        finish();
    }
    //endregion

    //region Toolbar Listener
    public void centerButtonClicked() {

    }
    public void leftButtonClicked() {
        handleBack();
    }
    public void rightButtonClicked() {

    }
    //endregion
}



