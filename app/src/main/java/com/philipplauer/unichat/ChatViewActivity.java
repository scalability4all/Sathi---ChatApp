package com.philipplauer.unichat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
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

import com.philipplauer.unichat.adapters.ChatMessagesAdapter;
import com.philipplauer.unichat.model.Chat;
import com.philipplauer.unichat.model.ChatMessagesModel;
import com.philipplauer.unichat.model.Contact;
import com.philipplauer.unichat.model.ContactModel;
import com.philipplauer.unichat.ui.KeyboardUtil;
import com.philipplauer.unichat.xmpp.RoosterConnectionService;

public class ChatViewActivity extends AppCompatActivity implements ChatMessagesAdapter.OnInformRecyclerViewToScrollDownListener,KeyboardUtil.KeyboardVisibilityListener,ChatMessagesAdapter.OnItemLongClickListener {
    private static final String LOGTAG = "ChatViewActivity" ;
    RecyclerView chatMessagesRecyclerView ;
    private EditText textSendEditText;
    private ImageButton sendMessageButton;
    ChatMessagesAdapter adapter;
    private String counterpartJid;
    private BroadcastReceiver mReceiveMessageBroadcastReceiver;
    private View snackbar;
    private View snackbarStranger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);
        Intent intent = getIntent();
        counterpartJid = intent.getStringExtra("contact_jid");
        Chat.ContactType chatType = (Chat.ContactType)intent.getSerializableExtra("chat_type");
        setTitle(counterpartJid);
        chatMessagesRecyclerView = findViewById(R.id.chatMessagesRecyclerView);
        chatMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        adapter = new ChatMessagesAdapter(getApplicationContext(),counterpartJid);
        adapter.setmOnInformRecyclerViewToScrollDownListener(this);
        adapter.setOnItemLongClickListener(this);
        chatMessagesRecyclerView.setAdapter(adapter);
        textSendEditText = findViewById(R.id.textinput);
        sendMessageButton = findViewById(R.id.textSendButton);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!textSendEditText.getText().toString().equals("")){
                    RoosterConnectionService.getConnection().sendMessage(textSendEditText.getText().toString(),counterpartJid);
                    adapter.onMessageAdd();
                    textSendEditText.getText().clear();
                }
            }
        });
        // Abfrage ob Kontakt fremd, entsprechende Snackbar einblenden
        snackbar = findViewById(R.id.snackbar);
        snackbarStranger = findViewById(R.id.snackbar_stranger);
        if( !ContactModel.get(getApplicationContext()).isContactStranger(counterpartJid))
        {
            snackbarStranger.setVisibility(View.GONE);
            Log.d(LOGTAG,counterpartJid + " kein Unbekannter");
            Contact contact = ContactModel.get(this).getContactByJidString(counterpartJid);
            Log.d(LOGTAG,"Kontakt mit JID :" + contact.getJid());
            if( contact.isPendingFrom())
            {
                Log.d(LOGTAG,"Abo von "+ contact.getJid() + " pending");
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.VISIBLE);
            }else
            {
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_normal);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.GONE);
            }
        }else
        {
            if(chatType == Chat.ContactType.STRANGER)
            {
                Log.d(LOGTAG,"Unbekannter");
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.VISIBLE);
                snackbarStranger.setVisibility(View.GONE);

            }else
            {
                Log.d(LOGTAG,counterpartJid + " ist unbekannt");
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbarStranger.setVisibility(View.VISIBLE);
                snackbar.setVisibility(View.GONE);
            }
        }
        TextView snackBarActionAccept = findViewById(R.id.snackbar_action_accept);
        snackBarActionAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContactModel.get(getApplicationContext()).isContactStranger(counterpartJid))
                {
                    if(ContactModel.get(getApplicationContext()).addContact(new Contact(counterpartJid, Contact.SubscriptionType.NONE)))
                    {
                        Log.d(LOGTAG,"Vorher unbekannt "+counterpartJid + "jetzt hinzugefügt");
                    }
                }
                Log.d(LOGTAG," Onlinestatus Abo accept :" + counterpartJid);
                if(RoosterConnectionService.getConnection().subscribed(counterpartJid))
                {
                    ContactModel.get(getApplicationContext()).updateContactSubscriptionOnSendSubscribed(counterpartJid);
                    Toast.makeText(ChatViewActivity.this,"Abo von "+counterpartJid + "akzeptiert",
                            Toast.LENGTH_LONG).show();
                }
                snackbar.setVisibility(View.GONE);

            }
        });

        TextView snackBarActionDeny = findViewById(R.id.snackbar_action_deny);
        snackBarActionDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOGTAG," Onlinestatus Abo abgelehnt :" + counterpartJid);
                if(RoosterConnectionService.getConnection().unsubscribed(counterpartJid))
                {
                    ContactModel.get(getApplicationContext()).updateContactSubscriptionOnSendSubscribed(counterpartJid);
                    Toast.makeText(getApplicationContext(),"Abo abgelehnt", Toast.LENGTH_LONG).show();
                }
                snackbar.setVisibility(View.GONE);
            }
        });
        TextView snackBarStrangerAddContact= findViewById(R.id.snackbar_action_accept_stranger);
        snackBarStrangerAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContactModel.get(getApplicationContext()).addContact(new Contact(counterpartJid, Contact.SubscriptionType.NONE)))
                {
                    if(RoosterConnectionService.getConnection().addContactToRoster(counterpartJid))
                    {
                        Log.d(LOGTAG,counterpartJid + " hinzugefügt");
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
        KeyboardUtil.setKeyboardVisibilityListener(this,this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_chat_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId() == R.id.contact_details_chat_view)
        {
            Intent i = new Intent(ChatViewActivity.this,ContactDetailsActivity.class);
            i.putExtra("contact_jid",counterpartJid);
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
        Log.d(LOGTAG, "new chatlist1");
        adapter.informRecyclerViewToScrollDown();
        mReceiveMessageBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(LOGTAG, "new chatlist2");
                switch (action)
                {
                    case Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG:
                        Log.d(LOGTAG, "new chatlist3");
                        adapter.onMessageAdd();
                        return;
                    case Constants.BroadCastMessages.UI_ONLINE_STATUS_CHANGE:
                        String contactJid = intent.getStringExtra(Constants.ONLINE_STATUS_CHANGE_CONTACT);
                        Log.d(LOGTAG," Onlinestatuswechsel "+contactJid + " bekommen");
                }
            }
        };
        IntentFilter filter = new IntentFilter(Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
        filter.addAction(Constants.BroadCastMessages.UI_ONLINE_STATUS_CHANGE);
        registerReceiver(mReceiveMessageBroadcastReceiver,filter);
    }
    @Override
    public void onInformRecyclerViewToScrollDown(int size) {
        chatMessagesRecyclerView.scrollToPosition(size-1);

    }
    @Override
    public void onKeyboardVisibilityChanged(boolean keyboardVisible) {
        adapter.informRecyclerViewToScrollDown();
    }
    @Override
    public void onItemLongClick(final  int uniqueId, View anchor) {
        PopupMenu popup = new PopupMenu(ChatViewActivity.this,anchor, Gravity.CENTER);
        popup.getMenuInflater().inflate(R.menu.chat_view_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch( item.getItemId())
                {
                    case R.id.delete_message :
                        if(ChatMessagesModel.get(getApplicationContext()).deleteMessage(uniqueId) )
                        {
                            adapter.onMessageAdd();
                            Toast.makeText(ChatViewActivity.this,"Nachricht gelöscht", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
}