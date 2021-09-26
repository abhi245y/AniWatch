package com.abhi245y.aniwatch.datamodels;

import android.graphics.Bitmap;

import com.google.android.exoplayer2.SimpleExoPlayer;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EpisodesListRVDataModel {

    String url, epNum;
    Bitmap thumpBit;
    SimpleExoPlayer simpleExoPlayer;
}
