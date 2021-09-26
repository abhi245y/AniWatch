package com.abhi245y.aniwatch.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.backend.DownloadUpdateService;
import com.abhi245y.aniwatch.datamodels.AniApiServerStatus;
import com.abhi245y.aniwatch.services.AniWatchApiService;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    AniWatchApiService aniWatchApiService;
    ProgressBar appLoading;
    TextView loading_status;

    private static final int STORAGE_PERMISSION_CODE = 101;

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                checkPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
            }else {
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
            }

        }
    };
    BroadcastReceiver onNotificationClick=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                checkPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
            }else {
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        appLoading = findViewById(R.id.app_loading);
        loading_status = findViewById(R.id.loading_status);

        String BASE_URL_ANI_API = "https://aniwatch-api.herokuapp.com/";
        Retrofit retrofitAni = new Retrofit.Builder()
                .baseUrl(BASE_URL_ANI_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        aniWatchApiService = retrofitAni.create(AniWatchApiService.class);

        startServer();

    }

    private void startServer() {
        Call<AniApiServerStatus> aniApiServerStatusCall = aniWatchApiService.getStatus();

        aniApiServerStatusCall.enqueue(new Callback<AniApiServerStatus>() {

            @Override
            public void onResponse(Call<AniApiServerStatus> call, Response<AniApiServerStatus> response) {
                assert response.body() != null;
                if(response.body().getResponse().equals("Server Started")){
                    appLoading.setVisibility(View.GONE);
                    loading_status.setText("Checking for update");
                    checkUpdate(response.body().getVersionCode(), response.body().getLink());
                }

            }

            @Override
            public void onFailure(Call<AniApiServerStatus> call, Throwable t) {
                startServer();
            }
        });
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this,
                    new String[] { permission },
                    requestCode);
            Toast.makeText(MainActivity.this, "Please grand permission", Toast.LENGTH_SHORT).show();


        }
        else {
            Toast.makeText(this,
                    "Necessary permissions granted Starting App Installation",
                    Toast.LENGTH_SHORT)
                    .show();
            installApk();
        }
    }

    private void checkUpdate(String versionCode, String link) {
        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            int verCode = pInfo.versionCode;
            if (verCode < Integer.parseInt(versionCode)){
                loading_status.setText("Downloading Update now Please wait..");
                Log.d("MainActivity","New Update Found Version Code: "+versionCode+"\nLink: "+link);

                startService(DownloadUpdateService.getDownloadService(this, link));
                registerReceiver(onComplete,
                        new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                registerReceiver(onNotificationClick,
                        new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
            }else{
                loading_status.setText("Launching App");
                Intent intent = new Intent(MainActivity.this, RecentReleasedActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void installApk() {
        try {
            loading_status.setText("Launching Installer");
            Context mContext = getApplicationContext();
            String PATH = "/storage/emulated/0/Android/data/"+getApplicationContext().getPackageName()+"/files/"+ Environment.DIRECTORY_DOWNLOADS;
            File file = new File(PATH + "/AniWatch.apk");
            Log.d("Location", "Path:"+file+" \nFile: "+PATH);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri downloaded_apk = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(downloaded_apk, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}