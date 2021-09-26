package com.abhi245y.aniwatch.backend;

import android.content.Context;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

public class VideoCache {
    private static SimpleCache sDownloadCache;

    public static SimpleCache getInstance(Context context) {
        if (sDownloadCache == null) sDownloadCache = new SimpleCache(new File(context.getCacheDir(), "exoCache"), new NoOpCacheEvictor(), new ExoDatabaseProvider(context));
        return sDownloadCache;
    }
}