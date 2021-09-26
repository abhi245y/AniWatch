package com.abhi245y.aniwatch.backend;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;


public class DownloadUpdateService extends IntentService {

    private static final String DOWNLOAD_PATH = "com.abhi245y.aniwatch_DownloadUpdateService_Download_path";

    public DownloadUpdateService() {
        super("DownloadSongService");
    }

    public static Intent getDownloadService(final @NonNull Context callingClassContext, final @NonNull String downloadPath) {
        return new Intent(callingClassContext, DownloadUpdateService.class)
                .putExtra(DOWNLOAD_PATH, downloadPath);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String downloadPath = intent.getStringExtra(DOWNLOAD_PATH);
        try {
            startDownload(downloadPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startDownload(String downloadPath) throws IOException {
        String PATH = "/storage/emulated/0/Android/data/"+getApplicationContext().getPackageName()+"/files/"+Environment.DIRECTORY_DOWNLOADS;
        File file = new File(PATH + "/AniWatch.apk");
        file.delete();
        if(file.exists()){
            file.getCanonicalFile().delete();
            if(file.exists()){
                file.delete();
                getApplicationContext().deleteFile(file.getName());
            }
        }
        Uri uri = Uri.parse(downloadPath); // Path where you want to download file.
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  // This will show notification on top when downloading the file.
        request.setTitle("Downloading Update");// Title for notification.
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOWNLOADS,"AniWatch.apk");  // Storage directory path
        ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request); // This will start downloading
    }
}