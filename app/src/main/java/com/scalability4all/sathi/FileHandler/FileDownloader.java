package com.scalability4all.sathi.FileHandler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonElement;
import com.scalability4all.sathi.ChatViewActivity;
import com.scalability4all.sathi.adapters.ChatMessagesAdapter;
import com.scalability4all.sathi.model.ChatMessage;
import com.scalability4all.sathi.model.ChatMessagesModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

import static com.google.firebase.crashlytics.internal.Logger.TAG;

public class FileDownloader {

    //public FileDownloaderCallback fileDownloaderCallback;
    Context context;
    TextView txtProgressPercent;
    ProgressBar progressBar;
    Button btnDownloadFile;

    DownloadZipFileTask downloadZipFileTask;

    public FileDownloader(Context context){

        this.context=context;
    }

    public interface downloadInterface {

        @Streaming
        @GET
        Call<ResponseBody> downloadFile(@Url String url, @Header("Authorization") String authorization);

        @Streaming
        @GET
        Call<ResponseBody> downloadFile(@Url String url);
    }
    public void downloadZipFile(String filepath, String filename, ChatMessage message, ChatMessagesAdapter adapter) {

        downloadInterface downloadService = ApiClient.getClient().create(downloadInterface.class);
        Call<ResponseBody> call = downloadService.downloadFile("download/"+filepath);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Got the body for the file");

                    Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();

                    downloadZipFileTask = new DownloadZipFileTask();
                    downloadZipFileTask.filename = filename;
                    downloadZipFileTask.message = message;
                    downloadZipFileTask.adapter = adapter;
                    downloadZipFileTask.execute(response.body());

                } else {
                    Log.d(TAG, "Connection failed " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e(TAG, t.getMessage());
            }
        });
    }

    private class DownloadZipFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {
        String filename;
        ChatMessage message;
        ChatMessagesAdapter adapter;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(ResponseBody... urls) {
            //Copy you logic to calculate progress and call
            saveToDisk(urls[0], filename);
            return null;
        }

        protected void onProgressUpdate(Pair<Integer, Long>... progress) {

            Log.d("API123", progress[0].second + " ");

            if (progress[0].first == 100)
                Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT).show();


            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
                //progressBar.setProgress(currentProgress);

                //txtProgressPercent.setText("Progress " + currentProgress + "%");

            }

            if (progress[0].first == -1) {
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
            }

        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {
            Helpers h = new Helpers();
            String download_message = h.MakeDownloadMessage(message.getMessage());
            message.setMessage(download_message);
            ChatMessagesModel.get(adapter.getContext()).updateMessage(message);
            adapter.onMessageAdd();
        }
    }

    private void saveToDisk(ResponseBody body, String filename) {
        try {

            File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
                Log.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    //downloadZipFileTask.doProgress(pairs);
                    Log.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }

                outputStream.flush();

                Log.d(TAG, destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                //downloadZipFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                //downloadZipFileTask.doProgress(pairs);
                Log.d(TAG, "Failed to save the file!");
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to save the file!");
            return;
        }
    }

}