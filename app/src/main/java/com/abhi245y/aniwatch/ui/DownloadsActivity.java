package com.abhi245y.aniwatch.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abhi245y.aniwatch.Adaptors.FileAdapter;
import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.backend.ActionListener;
import com.abhi245y.aniwatch.backend.Utils;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2core.Func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

@SuppressLint("SetTextI18n")
public class DownloadsActivity extends AppCompatActivity implements ActionListener {

    private static final long UNKNOWN_REMAINING_TIME = -1;
    private static final long UNKNOWN_DOWNLOADED_BYTES_PER_SECOND = 0;

    View mainView;
    FileAdapter fileAdapter;
    private Fetch fetch;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donwloads);
        setUpViews();
        fetch = Fetch.Impl.getDefaultInstance();
        fetch.setGlobalNetworkType(NetworkType.ALL);
        fetchDownloads();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setUpViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mainView = findViewById(R.id.downloads_main_lay);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(this);
        recyclerView.setAdapter(fileAdapter);

        Button deleteAll = findViewById(R.id.clear_downloads);

        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDownloadedFiles();
                fileAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDownloads();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        fetch.close();
    }

    @Override
    public void onPauseDownload(int id) {
        fetch.pause(id);
    }

    @Override
    public void onResumeDownload(int id) {
        fetch.resume(id);
    }

    @Override
    public void onRemoveDownload(int id) {
        fetch.remove(id);
    }

    @Override
    public void onRetryDownload(int id) {
        fetch.retry(id);
    }

    void fetchDownloads(){
        fetch.getDownloads(new Func<List<Download>>() {
            @Override
            public void call(@NonNull List<Download> result) {
                for (Download download : result) {
                    fileAdapter.addDownload(download);
                    Log.d("DownloadAct","List Result: "+download.getFile());
                }
            }
        }).addListener(fetchListener);
        fileAdapter.notifyDataSetChanged();
    }

    private final FetchListener fetchListener = new AbstractFetchListener() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onAdded(@NotNull Download download) {
            fileAdapter.addDownload(download);
        }

        @Override
        public void onQueued(@NotNull Download download, boolean waitingOnNetwork) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onCompleted(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
            super.onError(download, error, throwable);
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onProgress(@NotNull Download download, long etaInMilliseconds, long downloadedBytesPerSecond) {
            fileAdapter.update(download, etaInMilliseconds, downloadedBytesPerSecond);
        }

        @Override
        public void onPaused(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onResumed(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onCancelled(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onRemoved(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onDeleted(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }
    };

    private void deleteDownloadedFiles() {

        final FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this).setNamespace(AniWatch.FETCH_NAMESPACE).build();
        Fetch.Impl.getDefaultInstance().deleteAll().close();

        try {
            final File fetchDir = new File("/storage/emulated/0/AniWatch/");
            Utils.deleteFileAndContents(fetchDir);
            Toast.makeText(DownloadsActivity.this, R.string.downloaded_files_deleted, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}