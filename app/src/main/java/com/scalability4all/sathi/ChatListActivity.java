package com.scalability4all.sathi;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scalability4all.sathi.adapters.ChatListAdapter;
import com.scalability4all.sathi.model.Chat;
import com.scalability4all.sathi.model.ChatModel;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.jid.util.JidUtil;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class ChatListActivity extends AppCompatActivity implements ChatListAdapter.OnItemClickListener, ChatListAdapter.OnItemLongClickListener {
    protected static final int REQUEST_EXCEMPT_OP = 188;
    private static final String LOGTAG = "ChatListActivity";
    ChatListAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        setTitle(R.string.chatListTitle);
        getContactPermission();
        //Loginstatus abrufen, Falls nicht eingeloggt zurück zum LoginScreen
        boolean logged_in_state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("xmpp_logged_in", false);
        if (!logged_in_state) {
            Log.d(LOGTAG, "Login Status :" + logged_in_state);
            Intent i = new Intent(ChatListActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            if (!Utilities.isServiceRunning(RoosterConnectionService.class, getApplicationContext())) {
                Log.d(LOGTAG, "Service off, will start");
                Intent i1 = new Intent(this, RoosterConnectionService.class);
                startService(i1);
            } else {
                Log.d(LOGTAG, "Service is running");
            }
        }
        RecyclerView chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        mAdapter = new ChatListAdapter(getApplicationContext());
        mAdapter.setmOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        chatsRecyclerView.setAdapter(mAdapter);
        FloatingActionButton newConversationButton = findViewById(R.id.new_conversation_floating_button);
        newConversationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChatListActivity.this, ContactListActivity.class);
                startActivity(i);
            }
        });
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
                switch (action) {
                    case Constants.BroadCastMessages.UI_NEW_CHAT_ITEM:
                        mAdapter.onChatCountChange();
                        return;
                    case Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG:
                        String JabberId = intent.getStringExtra("JabberId");
                        mAdapter.OnNewMessage(JabberId);
                        Log.d(LOGTAG, "hi i am there "+JabberId);
                        return;
                }
            }
        };
        IntentFilter filter = new IntentFilter(Constants.BroadCastMessages.UI_NEW_CHAT_ITEM);
        filter.addAction(Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_me_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    //    if (item.getItemId() == R.id.me) {
      //      Intent i = new Intent(ChatListActivity.this, MeActivity.class);
        //    startActivity(i);
        if (item.getItemId() == R.id.logout) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            prefs.edit().clear().commit();
            try {
                Intent i1 = new Intent(this, RoosterConnectionService.class);
                stopService(i1);
                //Register or UnRegister your broadcast receiver here
                unregisterReceiver(mBroadcastReceiver);
            } catch(IllegalArgumentException e) {

                e.printStackTrace();
            }
            Intent l = new Intent(ChatListActivity.this, LoginActivity.class);
            startActivity(l);
            finish();
        }  else if(item.getItemId() == R.id.settings) {
            Intent l = new Intent(ChatListActivity.this, Settings.class);
            startActivity(l);
        }   else if (item.getItemId() != R.id.logout) { //R.id.groupchat
            // Gruppenchat Dialog
            final Dialog dialog = new Dialog(ChatListActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.groupchat_dialog);
            Button groupcreate = dialog.findViewById(R.id.groupchat_d_create);
            Button groupcancel = dialog.findViewById(R.id.groupchat_d_cancel);
            final EditText groupchatname = dialog.findViewById(R.id.groupchat_d_chatname);
            final Spinner member1 = dialog.findViewById(R.id.groupchat_d_spin1);
            //Spinner wird mit Testwerten gefüllt
            List<String> spinArray = new ArrayList<>();
            spinArray.add("");
            spinArray.add("test");
            spinArray.add("test2");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinArray);
            member1.setAdapter(adapter);
            groupcreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!groupchatname.getText().toString().equals("") && !member1.getSelectedItem().toString().equals("")) {
                        String serverurl = "localhost";
                        try {
                            //XMPPConnection ohne login nur für Gruppenchat
                            XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                                    .setXmppDomain(serverurl)
                                    .setHost(getResources().getString(R.string.xmpp_host))
                                    .setResource("sathi")
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
                                                createGroupChat(mConnection);
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
                        } catch (XmppStringprepException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    } else if (groupchatname.getText().toString().equals("")) {
                        groupchatname.setHint("Bitte Gruppennamen angeben");
                        groupchatname.setHintTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else if (member1.getSelectedItem().toString().equals("")) {
                        TextView bg = dialog.findViewById(R.id.groupchat_d_spinback);
                        bg.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    }
                }
            });
            groupcancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    //Aufruf eines Chats bei Klick
    @Override
    public void onItemClick(String contactJid, Chat.ContactType chatType) {
        Intent i = new Intent(ChatListActivity.this, ChatViewActivity.class);
        i.putExtra("contact_jid", contactJid);
        i.putExtra("chat_type", chatType);
        startActivity(i);
    }

    // Optionsmenu bei Chat nach langem Klick
    @Override
    public void onItemLongClick(final String contactJid, final int chatUniqueId, View anchor) {
        PopupMenu popup = new PopupMenu(ChatListActivity.this, anchor, Gravity.CENTER);
        popup.getMenuInflater().inflate(R.menu.chat_list_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_chat:
                        if (ChatModel.get(getApplicationContext()).deleteChat(chatUniqueId)) {
                            mAdapter.onChatCountChange();
                            Toast.makeText(ChatListActivity.this, "Chat erfolgreich gelöscht", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    // Ein Versuch zum Erstellen eines GroupChat auf dem Openfire Server
    // Leider ohne Funktion. Nähere Details im Projektbericht
    public void createGroupChat(XMPPConnection connection) {
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
        try {
            EntityBareJid jid = JidCreate.entityBareFrom("bob");
            MultiUserChat muc = manager.getMultiUserChat(jid);
            Set<Jid> owners = JidUtil.jidSetFrom(new String[]{"bob@localhost"});
            Resourcepart nickname = Resourcepart.from("bob");
            muc.create(nickname).getConfigFormManager().setRoomOwners(owners).submitConfigurationForm();
        } catch (XmppStringprepException | MultiUserChatException.MucAlreadyJoinedException | InterruptedException | XMPPException.XMPPErrorException | MultiUserChatException.MissingMucCreationAcknowledgeException |
                SmackException.NotConnectedException | SmackException.NoResponseException | MultiUserChatException.NotAMucServiceException | MultiUserChatException.MucConfigurationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void getContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    101);
        }
    }
}