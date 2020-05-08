package com.scalability4all.sathi;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.scalability4all.sathi.services.VolleyCallback;
import com.scalability4all.sathi.services.VolleyService;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.scalability4all.sathi.services.VolleyService.REGISTER_USER;
import static com.scalability4all.sathi.services.VolleyService.UPDATE_USER_PREFERENCE_LANGUAGE;

public class Register extends AppCompatActivity {
    private static final String LOGTAG = "Register";
    EditText username;
    EditText password;
    EditText confirm_password;
    EditText email;
    EditText language;
    Button register;
    private BroadcastReceiver mBroadcastReceiver;
    TextView login;
    Timer timer;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private CharSequence[] languages ;
    private String selectedLanguage;
    Map<CharSequence, String> languages_locale;
    View mProgressView;
    View mregisterFormView;
    TextView registration_error;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgressView = findViewById(R.id.register_progress);
        mregisterFormView=findViewById(R.id.register_form);

        registration_error=(TextView)findViewById(R.id.error_message);

        languages_locale=Constants.languages_locale;

        languages= languages_locale.keySet().toArray(new CharSequence[0]);

        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        confirm_password=findViewById(R.id.confirm_password);
        confirm_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(timer != null)
                    timer.cancel();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 3) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                        }

                    }, 3000);
                }
            }
        });
        confirm_password.setOnEditorActionListener(new EditText.OnEditorActionListener() {

           @Override
           public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
               if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                       actionId == EditorInfo.IME_ACTION_DONE ||
                       keyEvent != null &&
                               keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                               keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                   if (keyEvent == null || !keyEvent.isShiftPressed()) {
                       // the user is done typing.
                       String pwd = password.getText().toString();
                       String cpwd = confirm_password.getText().toString();
                       if(!pwd.equals(cpwd)) {
                           confirm_password.setError(getString(R.string.passwords_not_matching));
                       }
                       return true; // consume.
                   }
               }
               return false;
           }
        });

        email=findViewById(R.id.email);


        language=findViewById(R.id.language);
        language.setInputType(InputType.TYPE_NULL);

        language.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //dialogue popup
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Register.this)
                            .setTitle(R.string.choose_language)
                            .setPositiveButton(R.string.save, null)
                            .setNeutralButton(R.string.cancel, null)
                            .setSingleChoiceItems(languages, Arrays.asList(languages).indexOf(selectedLanguage) , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    selectedLanguage=new String((String) languages[i]);
                                }
                            });
                    final AlertDialog dialog=alertDialogBuilder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {

                            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    language.setText(selectedLanguage);
                                    dialog.dismiss();
                                }
                            });
                            Button close = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                            close.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    dialog.show();
                }
                return false;
            }
        });
        register=findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name=username.getText().toString();
                String pwd = password.getText().toString();
                String cpwd = confirm_password.getText().toString();

                String mailId=email.getText().toString();
                String lng=language.getText().toString();
                boolean doNotRegister = false;
                if(TextUtils.isEmpty(name)) {
                    username.setError(getString(R.string.error_field_required));
                    doNotRegister=true;
                }
                if(TextUtils.isEmpty(pwd)) {
                    password.setError(getString(R.string.error_field_required));
                    doNotRegister=true;
                }
                if (!TextUtils.isEmpty(pwd) && !isPasswordValid(pwd)) {
                    password.setError(getString(R.string.error_invalid_password));
                    doNotRegister=true;
                }
                if(TextUtils.isEmpty(mailId)) {
                    email.setError(getString(R.string.error_field_required));
                    doNotRegister=true;
                }
                if(!mailId.matches(emailPattern) && !TextUtils.isEmpty(mailId)) {
                    email.setError(getString(R.string.error_incorrect_gmail));
                    doNotRegister=true;
                }
                if(TextUtils.isEmpty(lng)) {
                    language.setText(getString(R.string.error_field_required));
                    doNotRegister=true;
                }
                if(!pwd.equals(cpwd)) {
                    confirm_password.setError(getString(R.string.passwords_not_matching));
                    doNotRegister=true;
                }
                if(!doNotRegister) {
                    register(name,pwd,mailId,lng);
                    showProgress(true);
                }
            }
        });

        login=findViewById(R.id.link_to_signin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Register.this,LoginActivity.class);
                startActivity(i);
            }
        });


    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mregisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {

            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(mBroadcastReceiver);
        } catch(IllegalArgumentException e) {

            e.printStackTrace();
        }

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action)
                {
                    case Constants.BroadCastMessages.UI_AUTHENTICATED:
                        Log.d(LOGTAG,"Mainscreen opens\n");
                         showProgress(false);
                        Intent i = new Intent(getApplicationContext(),ChatListActivity.class);
                        startActivity(i);
                        finish();
                        break;
                    case Constants.BroadCastMessages.UI_CONNECTION_ERROR:
                        Log.d(LOGTAG,"Connection Error");
                         showProgress(false);
                        // mJidView.setError("Login problems. Please check your details and try again.");
                        break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BroadCastMessages.UI_AUTHENTICATED);
        filter.addAction(Constants.BroadCastMessages.UI_CONNECTION_ERROR);
        this.registerReceiver(mBroadcastReceiver, filter);
    }

    private void register(final String name, final String password, final String emailId, final String language) {
        HashMap data = new HashMap();
        data.put("username", name);
        data.put("password", password);
        data.put("language",languages_locale.get(language));
        data.put("email",emailId);
        new VolleyService(new VolleyCallback() {
            @Override
            public void notifySuccess(JSONObject response) throws JSONException {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Register.this);
                prefs.edit()
                        .putString("xmpp_jid", name+"@"+getString(R.string.domain))
                        .putString("xmpp_password",password)
                        .putString("xmpp_email",emailId)
                        .putString("language", language)
                        .putString("category", "")
                        .commit();
        
                Intent i1 = new Intent(Register.this, RoosterConnectionService.class);
                startService(i1);

            }
            @Override
            public void notifyError(JSONObject error) {
                try {
                    showProgress(false);
                    JSONObject errorData=new JSONObject(error.getString("errors"));

                    if(errorData.getString("username")!=null && errorData.getString("username").length()>0) {
                        JSONArray usernameErrors=new JSONArray(errorData.getString("username"));
                        StringBuilder username_errors = new StringBuilder("");
                        for (int i=0; i<usernameErrors.length(); i++) {
                            username_errors.append(usernameErrors.get(i));
                            if((usernameErrors.length()-1)!=i) {
                                username_errors.append(",");
                            }
                        }
                        username.setError("Username " + String.valueOf(username_errors));
                    } else if(errorData.getString("email")!=null && errorData.getString("email").length()>0) {
                        JSONArray emailErrors=new JSONArray(errorData.getString("email"));
                        StringBuilder email_errors = new StringBuilder("");
                        for (int i=0; i<emailErrors.length(); i++) {
                            email_errors.append(emailErrors.get(i));
                            if(emailErrors.length()-1!=i) {
                                email_errors.append(",");
                            }
                        }
                        email.setError("Email " + String.valueOf(email_errors));
                    } else{
                        registration_error.setText((CharSequence) errorData);
                    }

                    Log.d(LOGTAG,"Unable to register");
                    Log.d(LOGTAG,error.getString("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, Register.this).postDataVolley(REGISTER_USER,data);
    }
}
