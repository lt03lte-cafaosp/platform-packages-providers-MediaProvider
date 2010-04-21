/* //device/content/providers/media/src/com/android/providers/media/MediaScannerReceiver.java
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package com.android.providers.media;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;

import java.io.File;


public class MediaScannerReceiver extends BroadcastReceiver
{
    private final static String TAG = "MediaScannerReceiver";
    private final static String usbMountPath = "/disk/media";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Uri uri = intent.getData();
        String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
        String externalDataPath = Environment.getDataDirectory() + usbMountPath;

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // scan internal storage
            scan(context, MediaProvider.INTERNAL_VOLUME);
        } else {
            if (uri.getScheme().equals("file")) {
                // handle intents related to external storage
                String path = uri.getPath();
                String dataPath = new String();
                /*
                 * Get the substring from intent for USB mass storage mount path
                 */
                 if (path.length() > externalDataPath.length()) {
                     dataPath = path.substring(0,externalDataPath.length());
                 }
                if (action.equals(Intent.ACTION_MEDIA_MOUNTED) && 
                        externalStoragePath.equals(path)) {
                    scan(context, MediaProvider.EXTERNAL_VOLUME);
                } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE) &&
                        path != null && path.startsWith(externalStoragePath + "/")) {
                    scanFile(context, path);
                } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED) &&
                        externalDataPath.equals(dataPath)) {
                        /*
                         * Scan the USB mass storage media If the event received is
                         * ACTION_MEDIA_MOUNTED and mount path is /data/disk/media
                         */
                        scan(context, MediaProvider.INTERNAL_VOLUME);
                }
            }
        }
    }

    private void scan(Context context, String volume) {
        Bundle args = new Bundle();
        args.putString("volume", volume);
        context.startService(
                new Intent(context, MediaScannerService.class).putExtras(args));
    }    

    private void scanFile(Context context, String path) {
        Bundle args = new Bundle();
        args.putString("filepath", path);
        context.startService(
                new Intent(context, MediaScannerService.class).putExtras(args));
    }    
}


