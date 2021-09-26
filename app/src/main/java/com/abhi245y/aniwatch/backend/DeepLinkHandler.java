package com.abhi245y.aniwatch.backend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.abhi245y.aniwatch.R;

import java.util.List;

public class DeepLinkHandler extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link_handler);

        handleIntent(getIntent());


    }


    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
            String recipeId = appLinkData.getLastPathSegment();
            Uri appData = Uri.parse("https://aniwatch-api.herokuapp.com/share/").buildUpon()
                    .appendPath(recipeId).build();
            TextView textView = findViewById(R.id.testText);

            List<String> parameters = appData.getPathSegments();


            for(String d : appData.getPathSegments()){
                textView.setText(d);
            }
        }
    }

}