package com.scalability4all.sathi.FileHandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.util.IOUtils;
import com.scalability4all.sathi.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Streaming;

public class Helpers {
    public Bitmap getBitmap(String path) {
        Bitmap bitmap=null;
        try {
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap ;
    }
    public Bitmap getBitmap(File f) {
        Bitmap bitmap=null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap ;
    }
    public Boolean IsUploadMessage(String message) {
        String package_name = "com.";
        if (message.length() == 0){
            return true;
        }
        if (message.contains(package_name) && message.contains("SEND_FILE")){
            return true;
        }
        return false;
    }
    public Boolean IsDownloadmessage(String message) {
        String package_name = "com.";
        if (message.length() == 0){
            return true;
        }
        if (message.contains(package_name) && message.contains("DOWNLOAD_FILE")){
            return true;
        }
        return false;
    }

    public String GetUriFromUpload(String message){
        String[] array = message.split(",");
        if(array.length > 1){
            return array[array.length - 2];
        }
        return null;
    }

    public String GetFileFromUpload(String message){
        String[] array = message.split(",");
        if(array.length > 0){
            return array[array.length - 1];
        }
        return null;
    }

    public String GetTrFromUpload(String message){
        String[] array = message.split(",");
        if(array.length > 0){
            return array[0];
        }
        return null;
    }

    public String GetExtFromUpload(String message){
        String name = GetFileFromUpload(message);
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    public String GetDownloadFileName(String message){
        String name = GetTrFromUpload(message);
        String ext = GetExtFromUpload(message);
        return name + ext;
    }

    public void setImage(Bitmap bmp, TextView tv)
    {
        tv.setTransformationMethod(null);
        SpannableString ss = new SpannableString("  ");
        ss.setSpan(new ImageSpan(bmp, ImageSpan.ALIGN_BASELINE), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv.setText(ss);
    }

    public String MakeDownloadMessage(String message)
    {
        return message.replace("SEND_FILE", "DOWNLOAD_FILE");
    }
}
