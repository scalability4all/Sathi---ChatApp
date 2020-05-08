package com.scalability4all.sathi;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.scalability4all.sathi.services.VolleyCallback;
import com.scalability4all.sathi.services.VolleyService;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.scalability4all.sathi.services.VolleyService.GET_USER_DETAILS;


public class LoginActivity extends AppCompatActivity {
    private static final String LOGTAG = "RoosterPlus";
    private TextInputEditText mJidView;
    private TextInputEditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private BroadcastReceiver mBroadcastReceiver;
    private String serverurl = "localhost";
    private TextView register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mJidView = findViewById(R.id.jid);
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        MaterialButton mJidSignInButton = findViewById(R.id.jid_sign_in_button);
        mJidSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        register=findViewById(R.id.link_to_register);
        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this,Register.class);
                startActivity(i);
            }
        });

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
                switch (action)
                {
                    case Constants.BroadCastMessages.UI_AUTHENTICATED:
                        Log.d(LOGTAG,"Mainscreen opens\n");
                        showProgress(false);
                        // getting user details
                        getUserPreferenceData(mJidView.getText().toString());
                        Intent i = new Intent(getApplicationContext(),ChatListActivity.class);
                        startActivity(i);
                        finish();
                        break;
                    case Constants.BroadCastMessages.UI_CONNECTION_ERROR:
                        Log.d(LOGTAG,"Connection Error");
                        showProgress(false);
                        mJidView.setError("Login problems. Please check your details and try again.");
                        break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BroadCastMessages.UI_AUTHENTICATED);
        filter.addAction(Constants.BroadCastMessages.UI_CONNECTION_ERROR);
        this.registerReceiver(mBroadcastReceiver, filter);
    }
    private void attemptLogin() {
        // Errors reset to default
        mJidView.setError(null);
        mPasswordView.setError(null);
        String jid = mJidView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        //Password check (length only here, not correct on server
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        // JID check (per @)
        if (TextUtils.isEmpty(jid)) {
            mJidView.setError(getString(R.string.error_field_required));
            focusView = mJidView;
            cancel = true;
        } else if (!isJidValid(jid)) {
            mJidView.setError(getString(R.string.error_invalid_jid));
            focusView = mJidView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            saveCredentialsAndLogin();
        }
    }
    private boolean isJidValid(String email) {
        return email.contains("@");
    }
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
    // Show progress bar and hide login
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    private void saveCredentialsAndLogin()
    {
        Log.d(LOGTAG,"saveCredentialsAndLogin() called.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("xmpp_jid", mJidView.getText().toString())
                .putString("xmpp_password", mPasswordView.getText().toString())
                .commit();

        Intent i1 = new Intent(this, RoosterConnectionService.class);
        startService(i1);
    }

    private void getUserPreferenceData(String username) {
        new VolleyService(new VolleyCallback() {
            @Override
            public void notifySuccess(JSONObject response) throws JSONException {
                try {
                    JSONObject data=new JSONObject(response.getString("data"));
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    List<CharSequence> newsCategory = new ArrayList<CharSequence>();
                    JSONArray category=new JSONArray(data.getString("category"));
                    StringBuilder categories = new StringBuilder("");
                    Map<CharSequence, String> languages_locale = Constants.languages_locale;

                    for (int i=0; i<category.length(); i++) {
                        categories.append(category.get(i));
                        if((category.length()-1)!=i) {
                            categories.append(",");
                        }
                    }
                    prefs.edit()
                            .putString("language", (String) Constants.getKeyByValue(languages_locale,data.getString("language")))
                            .putString("category", String.valueOf(categories))
                            .commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void notifyError(JSONObject error) {
                try {
                    Log.d(LOGTAG,"Parse error in getting user details");
                    Log.d(LOGTAG,error.getString("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },LoginActivity.this).getDataVolley(GET_USER_DETAILS+'/'+username.split("@")[0]);
    }

    private void saveCredentialsAndLoginR(String username, String password)
    {
        Log.d(LOGTAG,"saveCredentialsAndLogin() called.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("xmpp_jid", username)
                .putString("xmpp_password", password)
                .commit();
        Intent i1 = new Intent(this, RoosterConnectionService.class);
        startService(i1);
    }

    // Registrierung auf XMPP Server - Verbindung extra ohne Benutzerdaten
    private void attemptRegister(final String username, final String password) throws IOException{
        XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(serverurl)
                .setHost(getResources().getString(R.string.xmpp_host))
                .setResource("SATHI")
                .setKeystoreType(null)
                .setSendPresence(true)
                .setDebuggerEnabled(true)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setCompressionEnabled(true).build();
        SmackConfiguration.DEBUG = true;
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        final AbstractXMPPConnection mConnection = new XMPPTCPConnection(conf);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        try {
                            mConnection.connect();
                            AccountManager accountManager = AccountManager.getInstance(mConnection);
                            accountManager.sensitiveOperationOverInsecureConnection(true);
                            accountManager.createAccount(Localpart.from(username), password);
                            saveCredentialsAndLoginR(username, password);
                        } catch (SmackException | IOException | XMPPException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }



}