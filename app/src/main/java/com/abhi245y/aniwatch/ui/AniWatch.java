package com.abhi245y.aniwatch.ui;

import android.app.Application;

import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import io.realm.Realm;

public class AniWatch extends Application {

    public static final String FETCH_NAMESPACE = "AnimeDownload";
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(10)
                .setHttpDownloader(new OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
                .setNamespace(FETCH_NAMESPACE)
                .build();
        Fetch.Impl.setDefaultInstanceConfiguration(fetchConfiguration);

    }
}
