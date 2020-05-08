package com.scalability4all.sathi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scalability4all.sathi.adapters.ChatMessagesAdapter;
import com.scalability4all.sathi.model.Chat;
import com.scalability4all.sathi.model.ChatMessagesModel;
import com.scalability4all.sathi.model.Contact;
import com.scalability4all.sathi.model.ContactModel;
import com.scalability4all.sathi.ui.KeyboardUtil;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.scalability4all.sathi.Constants.removeHostNameFromJID;

public class ChatViewActivity extends AppCompatActivity implements ChatMessagesAdapter.OnInformRecyclerViewToScrollDownListener, KeyboardUtil.KeyboardVisibilityListener, ChatMessagesAdapter.OnItemLongClickListener {
    private static final String LOGTAG = "ChatViewActivity";
    RecyclerView chatMessagesRecyclerView;
    private EditText textSendEditText;
    private ImageButton sendMessageButton;
    ChatMessagesAdapter adapter;
    private String counterpartJid;
    private BroadcastReceiver mReceiveMessageBroadcastReceiver;
    private View snackbar;
    private static final int SPEECH_REQUEST_CODE = 10;
    private View snackbarStranger;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);


        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = sh.getString("xmpp_jid", null);

        Intent intent = getIntent();
        counterpartJid = intent.getStringExtra("contact_jid");
        Chat.ContactType chatType = (Chat.ContactType) intent.getSerializableExtra("chat_type");
        setTitle(removeHostNameFromJID(counterpartJid));
        chatMessagesRecyclerView = findViewById(R.id.chatMessagesRecyclerView);
        chatMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        adapter = new ChatMessagesAdapter(getApplicationContext(), counterpartJid, username);
        adapter.setmOnInformRecyclerViewToScrollDownListener(this);
        adapter.setOnItemLongClickListener(this);
        chatMessagesRecyclerView.setAdapter(adapter);
        //ImageView sendButton = findViewById(R.id.send_btn);
        //ImageView cancelButton = findViewById(R.id.cancel_btn);
        ImageButton recordButton = findViewById(R.id.record_button);
        Map<String, String> languages = new HashMap<String, String>();
        languages.put("english", "en-IN");
        languages.put("hindi", "hi-IN");
        languages.put("punjabi", "pa-guru-IN");
        languages.put("tamil", "ta-IN");
        languages.put("telugu", "te-IN");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language = pref.getString("language", "english");
        final String lan_locale = languages.get(language);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lan_locale);
                startActivityForResult(intent, 10);
            }
        });
        final String contactName = ""; // TODO
        final String contactNumber = ""; //TODO
//        sendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog alertDialog = new AlertDialog.Builder(ChatViewActivity.this)
//                        .setMessage(contactName + " " + getString(R.string.whatsapp_dialog_message))
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }).setPositiveButton("Send", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                sendWhatsApp(contactNumber, "Hey, Sending message from Zing!");
//                            }
//                        })
//                        .create();
//                alertDialog.show();
//            }
//        });
        textSendEditText = findViewById(R.id.textinput);
        sendMessageButton = findViewById(R.id.textSendButton);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!textSendEditText.getText().toString().equals("")) {
                    RoosterConnectionService.getConnection().sendMessage(textSendEditText.getText().toString(), counterpartJid, username);
                    adapter.onMessageAdd();
                    textSendEditText.getText().clear();
                }
            }
        });

        // Abfrage ob Kontakt fremd, entsprechende Snackbar einblenden
        snackbar = findViewById(R.id.snackbar);
        snackbarStranger = findViewById(R.id.snackbar_stranger);
        if (!ContactModel.get(getApplicationContext()).isContactStranger(counterpartJid)) {
            snackbarStranger.setVisibility(View.GONE);
            Log.d(LOGTAG, counterpartJid + " no stranger");
            Contact contact = ContactModel.get(this).getContactByJidString(counterpartJid);
            Log.d(LOGTAG, "Contact me JID :" + contact.getJid());
            if (contact.isPendingFrom()) {
                Log.d(LOGTAG, "Subscription from " + contact.getJid() + " pending");
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0, 0, 0, paddingBottom);
                snackbar.setVisibility(View.VISIBLE);
            } else {
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_normal);
                chatMessagesRecyclerView.setPadding(0, 0, 0, paddingBottom);
                snackbar.setVisibility(View.GONE);
            }
        } else {
            if (chatType == Chat.ContactType.STRANGER) {
                Log.d(LOGTAG, "Unknown");
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0, 0, 0, paddingBottom);
                snackbar.setVisibility(View.VISIBLE);
                snackbarStranger.setVisibility(View.GONE);

            } else {
                Log.d(LOGTAG, counterpartJid + " is unknown");
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0, 0, 0, paddingBottom);
                snackbarStranger.setVisibility(View.VISIBLE);
                snackbar.setVisibility(View.GONE);
            }
        }
        TextView snackBarActionAccept = findViewById(R.id.snackbar_action_accept);
        snackBarActionAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContactModel.get(getApplicationContext()).isContactStranger(counterpartJid)) {
                    if (ContactModel.get(getApplicationContext()).addContact(new Contact(counterpartJid, Contact.SubscriptionType.NONE))) {
                        Log.d(LOGTAG, "Previously unknown " + counterpartJid + "added now");
                    }
                }
                Log.d(LOGTAG, " Online Status accept :" + counterpartJid);
                if (RoosterConnectionService.getConnection().subscribed(counterpartJid)) {
                    ContactModel.get(getApplicationContext()).updateContactSubscriptionOnSendSubscribed(counterpartJid);
                    Toast.makeText(ChatViewActivity.this, "Subscription from " + counterpartJid + "accepted",
                            Toast.LENGTH_LONG).show();
                }
                snackbar.setVisibility(View.GONE);

            }
        });

        TextView snackBarActionDeny = findViewById(R.id.snackbar_action_deny);
        snackBarActionDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOGTAG, " Online status subscription rejected :" + counterpartJid);
                if (RoosterConnectionService.getConnection().unsubscribed(counterpartJid)) {
                    ContactModel.get(getApplicationContext()).updateContactSubscriptionOnSendSubscribed(counterpartJid);
                    Toast.makeText(getApplicationContext(), "Subscription declined", Toast.LENGTH_LONG).show();
                }
                snackbar.setVisibility(View.GONE);
            }
        });
        TextView snackBarStrangerAddContact = findViewById(R.id.snackbar_action_accept_stranger);
        snackBarStrangerAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContactModel.get(getApplicationContext()).addContact(new Contact(counterpartJid, Contact.SubscriptionType.NONE))) {
                    if (RoosterConnectionService.getConnection().addContactToRoster(counterpartJid)) {
                        Log.d(LOGTAG, counterpartJid + " added");
                        snackbarStranger.setVisibility(View.GONE);
                    }
                }
            }
        });
        TextView snackBarStrangerBlock = findViewById(R.id.snackbar_action_deny_stranger);
        snackBarStrangerBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dismiss
            }
        });
        KeyboardUtil.setKeyboardVisibilityListener(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.contact_details_chat_view) {
            Intent i = new Intent(ChatViewActivity.this, ContactDetailsActivity.class);
            i.putExtra("contact_jid", counterpartJid);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiveMessageBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.informRecyclerViewToScrollDown();
        mReceiveMessageBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(LOGTAG, "new chatlist2");
                switch (action) {
                    case Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG:
                        Log.d(LOGTAG, "new chatlist3");
                        adapter.onMessageAdd();
                        return;
                    case Constants.BroadCastMessages.UI_ONLINE_STATUS_CHANGE:
                        String contactJid = intent.getStringExtra(Constants.ONLINE_STATUS_CHANGE_CONTACT);
                        Log.d(LOGTAG, " Online status change " + contactJid + " to get");
                }
            }
        };
        IntentFilter filter = new IntentFilter(Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
        filter.addAction(Constants.BroadCastMessages.UI_ONLINE_STATUS_CHANGE);
        registerReceiver(mReceiveMessageBroadcastReceiver, filter);
    }

    @Override
    public void onInformRecyclerViewToScrollDown(int size) {
        chatMessagesRecyclerView.scrollToPosition(size - 1);

    }

    @Override
    public void onKeyboardVisibilityChanged(boolean keyboardVisible) {
        adapter.informRecyclerViewToScrollDown();
    }

    @Override
    public void onItemLongClick(final int uniqueId, View anchor) {
        PopupMenu popup = new PopupMenu(ChatViewActivity.this, anchor, Gravity.CENTER);
        popup.getMenuInflater().inflate(R.menu.chat_view_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_message:
                        if (ChatMessagesModel.get(getApplicationContext()).deleteMessage(uniqueId)) {
                            adapter.onMessageAdd();
                            Toast.makeText(ChatViewActivity.this, "Message deleted", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void sendWhatsApp(String phone, String message) {
        PackageManager packageManager = this.getPackageManager();
        Intent i = new Intent(Intent.ACTION_VIEW);
        try {
            String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + URLEncoder.encode(message, "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                this.startActivity(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("speech2", String.valueOf(requestCode));
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> speechData = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (speechData.size() > 0) {
                String spokenText = speechData.get(0);
                EditText edit_text = findViewById(R.id.textinput);
                String current_text = edit_text.getText().toString();
                if (current_text.length() != 0) {
                    current_text = current_text + ".";
                }
                edit_text.setText(current_text + spokenText);
                Log.d("speech", speechData.toString());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}