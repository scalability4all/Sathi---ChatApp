package com.scalability4all.sathi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.scalability4all.sathi.xmpp.RoosterConnection;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class MeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOGTAG = "MeActivity";
    private BroadcastReceiver mBroadcastReceiver;
    private TextView connectionStatusTextView;
    private ImageView profileImageView;
    private static final int SELECT_PHOTO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        String status;
        RoosterConnection connection = RoosterConnectionService.getConnection();
        connectionStatusTextView = findViewById(R.id.connection_status);
        if (connection != null) {
            status = connection.getConnectionStateString();
            connectionStatusTextView.setText(status);
        }
        profileImageView = findViewById(R.id.profile_image);
        profileImageView.setOnClickListener(this);
        String selfJid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("xmpp_jid", null);
        RoosterConnection rc = RoosterConnectionService.getConnection();
        profileImageView.setImageResource(R.mipmap.ic_profile);
        if (rc != null) {
            String imageAbsPath = rc.getProfileImageAbsolutePath(selfJid);
            if (imageAbsPath != null) {
                Drawable d = Drawable.createFromPath(imageAbsPath);
                profileImageView.setImageDrawable(d);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Constants.BroadCastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG:
                        String status = intent.getStringExtra(Constants.UI_CONNECTION_STATUS_CHANGE);
                        connectionStatusTextView.setText(status);
                        break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BroadCastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG);
        this.registerReceiver(mBroadcastReceiver, filter);
    }

    //OnClick for profile pictures
    @Override
    public void onClick(View v) {
        Log.d(LOGTAG, "Profile picture clicked");
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    // Setzen neues Profilbild
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Log.d(LOGTAG, "Result OK");
                    Uri selectedImage = data.getData();
                    Bitmap bm = null;
                    try {
                        bm = decodeUri(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (bm != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.PNG, 0, stream);
                        byte[] byteArray = stream.toByteArray();
                        Log.d(LOGTAG, "Bitmap != NULL; Picture is set; Arraysize:" + byteArray.length);
                        RoosterConnection rc = RoosterConnectionService.getConnection();
                        if (rc != null) {
                            if (rc.setSelfAvatar(byteArray)) {
                                Log.d(LOGTAG, "Profile picture set");
                                //Set the avatar to be shown in the profile Image View
                                Drawable image = new BitmapDrawable(getResources(),
                                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
                                profileImageView.setImageDrawable(image);
                            } else {
                                Log.d(LOGTAG, "Profile picture could not be set");
                            }
                        }
                    }
                } else {
                    Log.d(LOGTAG, "Image selection canceled");
                }
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);
        // Required size
        final int REQUIRED_SIZE = 140;
        // Find Scale
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }
}
