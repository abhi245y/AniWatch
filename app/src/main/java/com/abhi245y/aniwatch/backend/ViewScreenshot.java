package com.abhi245y.aniwatch.backend;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.view.PixelCopy;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import javax.inject.Inject;

public class ViewScreenshot {

    public interface PostTake {

        void onSuccess(Bitmap bitmap);
        void onFailure(int error);
    }

    @Inject
    public ViewScreenshot() {

    }

    public void take(CoordinatorLayout playerControlsView, View view, Activity activity, PostTake callback) {


        if (callback == null) {

            throw new IllegalArgumentException("Screenshot request without a callback");
        }

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        int[] location = new int[2];
        view.getLocationInWindow(location);

        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        PixelCopy.OnPixelCopyFinishedListener listener = new PixelCopy.OnPixelCopyFinishedListener() {

            @Override
            public void onPixelCopyFinished(int copyResult) {

                if (copyResult == PixelCopy.SUCCESS) {

                    callback.onSuccess(bitmap);
                } else {

                    callback.onFailure(copyResult);
                }
            }
        };

        try {

            PixelCopy.request(activity.getWindow(), rect, bitmap, listener, new Handler());
        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        }
    }
}