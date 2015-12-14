/**
 * Copyright (c) 2015 unfoldingWord
 * http://creativecommons.org/licenses/MIT/
 * See LICENSE file for details.
 * Contributors:
 * PJ Fechner <pj@actsmedia.com>
 */

package com.github.peejweej.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.github.peejweej.androidsideloading.activities.SideLoadActivity;
import com.github.peejweej.androidsideloading.activities.SideShareActivity;
import com.github.peejweej.androidsideloading.model.SideLoadInformation;
import com.github.peejweej.androidsideloading.model.SideLoadVerifier;

import java.io.File;

/**
 * Created by Fechner on 8/24/15.
 */
public class SharingHelper {

    private static final String FILE_EXTENSION = ".txt";

    public static SideLoadInformation getLoadInformation(){
        return new SideLoadInformation(FILE_EXTENSION, new SideLoadVerifier() {
            @Override
            public boolean fileIsValid(String file) {
                return true;
            }

            @Override
            public boolean fileIsValid(Uri file) {
                return true;
            }
        });
    }

    private static SideLoadInformation getShareInformation(Uri fileUri, String fileName){
        return new SideLoadInformation(fileName, fileUri);
    }

    public static SideLoadInformation getShareInformation(Context context){

        Uri fileUri = getFileForVersion(context);

        return getShareInformation(fileUri, getFileName());
    }

    private static Uri getFileForVersion(Context context){

        return FileUtil.createTemporaryFile(context, "This is a test file", getFileName());
    }

    private static String getFileName(){
        return "Test File" + FILE_EXTENSION;
    }

    public static Intent getIntentForLoading(Context context){
        return new Intent(context, SideLoadActivity.class).putExtra(SideLoadActivity.SIDE_LOAD_INFORMATION_PARAM, getLoadInformation());
    }

    public static Intent getIntentForSharing(Context context){

        return new Intent(context, SideShareActivity.class).putExtra(SideShareActivity.SIDE_LOAD_INFORMATION_PARAM, getShareInformation(context));
    }
}