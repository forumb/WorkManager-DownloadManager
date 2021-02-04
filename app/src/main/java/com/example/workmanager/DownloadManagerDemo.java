package com.example.workmanager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Query;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class DownloadManagerDemo extends AppCompatActivity {

    private long downloadID;
    private DownloadManager downloadManager;


    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        try {
            if(checkPermission()) {
                beginDownload();
            }
            else requestPermission();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void beginDownload() throws IOException {

            String url = "http://androhub.com/demo/demo.zip";
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            fileName = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);

            String dmtTreePath = this.getFilesDir() + File.separator + "zip_files";
            File url_data = new File(dmtTreePath);

            //If File is not present create directory
            if (!url_data.exists()) {
                if (url_data.mkdir()) {
                    Log.e("TAG", "Directory Created.");
                }
            } else Log.e("TAG", "Directory already exist.");

            File outputFile = new File(url_data, fileName);
//        //Create New File if not present
//        if (!outputFile.exists()) {
//            if (outputFile.createNewFile()) {
//                Log.e("TAG", "File Created");
//            }
//        } else Log.e("TAG", "File already exist.");

            Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .mkdirs();

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)// Visibility of the download Notification
//                .setDestinationUri(Uri.fromFile(outputFile))// Uri of the destination file
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "demo.zip")
                    .setTitle(fileName)// Title of the Download Notification
                    .setDescription("Downloading")// Description of the Download Notification
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                            DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                    .setAllowedOverRoaming(true);// Set if download is allowed on roaming network

            downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.

        // using query method
        boolean finishDownload = false;
        int progress;
        while (!finishDownload) {
            Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadID));
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch (status) {
                    case DownloadManager.STATUS_FAILED: {
                        finishDownload = true;
                        break;
                    }
                    case DownloadManager.STATUS_PAUSED:
                        break;
                    case DownloadManager.STATUS_PENDING:
                        break;
                    case DownloadManager.STATUS_RUNNING: {
                        final long total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        if (total >= 0) {
                            final long downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            progress = (int) ((downloaded * 100L) / total);
                            Log.d("TAG", "Progress: "+ progress);
                        }
                        break;
                    }
                    case DownloadManager.STATUS_SUCCESSFUL: {
                        progress = 100;
                        finishDownload = true;
                        Toast.makeText(this, "Download Completed", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .");
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
            break;
        }
    }

}