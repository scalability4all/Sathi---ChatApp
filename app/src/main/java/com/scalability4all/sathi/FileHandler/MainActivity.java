package com.scalability4all.sathi.FileHandler;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scalability4all.sathi.ChatViewActivity;
import com.scalability4all.sathi.R;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;

import org.json.JSONObject;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity {

    ArrayList<String> files = new ArrayList<>();
    private ProgressDialog pDialog;
    // variable to hold context
    private Context context;
    private ChatViewActivity activity;
    //save the context recievied via constructor in a local variable

    public MainActivity(Context context, ChatViewActivity activity){

        this.context=context;
        this.activity=activity;
        pDialog = new ProgressDialog(this.context);
    }

    public ArrayList<String> GetFiles(){
        return files;
    }

    public void getImageFilePath(Uri uri) {

        File file = new File(uri.getPath());
        String[] filePath = file.getPath().split(":");
        String image_id = filePath[filePath.length - 1];
        Cursor cursor = this.context.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            files.add(imagePath);
            cursor.close();
        }
    }

    public void uploadFiles() {
        File[] filesToUpload = new File[files.size()];
        for (int i = 0; i < files.size(); i++) {
            Log.d("abc", files.get(i));
            filesToUpload[i] = new File(files.get(i));
        }
        showProgress("Uploading media ...");
        FileUploader fileUploader = new FileUploader();
        fileUploader.uploadFiles("/upload", "file", filesToUpload, new FileUploader.FileUploaderCallback() {
            @Override
            public void onError() {
                hideProgress();
            }

            @Override
            public void onFinish(String[] responses) {
                hideProgress();
                for (int i = 0; i < responses.length; i++) {
                    String str = responses[i];
                    File f = filesToUpload[i];
                    String file_path = f.getPath();
                    //Log.e("RESPONSE " + i, responses[i]);
                    JsonParser jsonParser = new JsonParser();
                    JsonObject json = (JsonObject) jsonParser.parse(str);
                    if(json != null && json.get("response") != null){
                        JsonObject resp = json.getAsJsonObject("response");
                        String status = resp.get("status").getAsString();
                        if(status.equalsIgnoreCase( "ok") ){
                            String file = resp.getAsJsonObject("data").get("file").getAsString();
                            String package_name = context.getResources().getString(R.string.package_name);
                            String uniqueID = UUID.randomUUID().toString();
                            String message = MessageFormat.format("[{0}],{1},{2},{3},{4}",uniqueID,package_name, "SEND_FILE", file_path, file);
                            activity.MessageSend(message);
                        }
                    }
                }
            }

            @Override
            public void onProgressUpdate(int currentpercent, int totalpercent, int filenumber) {
                updateProgress(totalpercent, "Uploading file " + filenumber, "");
                Log.e("Progress Status", currentpercent + " " + totalpercent + " " + filenumber);
            }
        });
    }

    public void updateProgress(int val, String title, String msg) {
        pDialog.setTitle(title);
        pDialog.setMessage(msg);
        pDialog.setProgress(val);
    }

    public void showProgress(String str) {
        try {
            pDialog.setCancelable(false);
            pDialog.setTitle("Please wait");
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setMax(100); // Progress Dialog Max Value
            pDialog.setMessage(str);
            if (pDialog.isShowing())
                pDialog.dismiss();
            pDialog.show();
        } catch (Exception e) {

        }
    }

    public void hideProgress() {
        try {
            if (pDialog.isShowing())
                pDialog.dismiss();
        } catch (Exception e) {

        }

    }
}