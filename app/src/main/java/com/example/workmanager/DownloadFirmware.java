package com.example.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class DownloadFirmware extends Worker {
    Context context;
    public DownloadFirmware(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String downloadUrl = getInputData().getString("Download_URL");

        try {
            downloadFota(downloadUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.success();
    }

    private void downloadFota(String downloadUrl) throws IOException {

        File url_data = null, outputFile = null;
        String downloadFileName = "fumo_flash_data.zip";

        URL url = new URL(downloadUrl);//Create Download URl
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        //If Connection response is not OK then show Logs
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            Log.e("TAG", "Server returned HTTP " + conn.getResponseCode()
                    + " " + conn.getResponseMessage());

        }

        // save path
        String dmtTreePath = context.getFilesDir() + File.separator + "zip_files";
        url_data = new File(dmtTreePath);

        //If File is not present create directory
        if (!url_data.exists()) {
            if(url_data.mkdir()) {
                Log.e("TAG", "Directory Created.");
            }
        }else Log.e("TAG", "Directory already exist.");

        outputFile = new File(url_data, downloadFileName);

        //Create New File if not present
        if (!outputFile.exists()) {
            if(outputFile.createNewFile()) {
                Log.e("TAG", "File Created");
            }
        }else Log.e("TAG", "File already exist.");

        FileOutputStream fos = new FileOutputStream(outputFile);
        InputStream is = conn.getInputStream();

        byte[] buffer = new byte[1024];
        int len1 = 0;
        while ((len1 = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len1);
        }

        //Close all connection
        fos.close();
        is.close();

    }
}
