package com.abhi245y.aniwatch.backend;

public interface ActionListener {

    void onPauseDownload(int id);

    void onResumeDownload(int id);

    void onRemoveDownload(int id);

    void onRetryDownload(int id);
}
