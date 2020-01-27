package com.philipplauer.unichat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.philipplauer.unichat.adapters.ContactListAdapter;
import com.philipplauer.unichat.model.Chat;
import com.philipplauer.unichat.model.ChatModel;
import com.philipplauer.unichat.model.Contact;
import com.philipplauer.unichat.model.ContactModel;
import com.philipplauer.unichat.xmpp.RoosterConnectionService;

import java.util.List;

public class ContactListActivity extends AppCompatActivity implements ContactListAdapter.OnItemClickListener ,ContactListAdapter.OnItemLongClickListener {
    ContactListAdapter mAdapter;
    private static final String LOGTAG = "ContactListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        setTitle(R.string.contactListTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton newContactButton = findViewById(R.id.new_contact_button);
        newContactButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
        newContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact();

            }
        });
        RecyclerView contactListRecyclerView = findViewById(R.id.contact_list_recycler_view);
        contactListRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        mAdapter = new ContactListAdapter(getApplicationContext());
        mAdapter.setmOnItemClickListener(this);
        mAdapter.setmOnItemLongClickListener(this);
        contactListRecyclerView.setAdapter(mAdapter);
    }
    // Kontakt hinzufügen
    private void addContact()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_contact_label_text);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton(R.string.add_contact_confirm_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(ContactModel.get(getApplicationContext()).addContact(new Contact(input.getText().toString(), Contact.SubscriptionType.NONE)))
                {
                    mAdapter.onContactCountChange();
                    Log.d(LOGTAG,"Kontakt hinzugefügt");
                }
                else
                {
                    Log.d(LOGTAG,"Kontakt konnte nicht hinzugefügt werden");
                }
            }
        });
        builder.setNegativeButton(R.string.add_contact_cancel_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOGTAG,"Abbruch durch Benutzer");
                dialog.cancel();
            }
        });
        builder.show();
    }
    @Override
    public void onItemClick(String contactJid) {
        Log.d(LOGTAG,"Geklickter Kontakt: "+contactJid);
        List<Chat> chats = ChatModel.get(getApplicationContext()).getChatsByJid(contactJid);
        if( chats.size() == 0)
        {
            Log.d(LOGTAG, contactJid + " neuer Chat. Timestamp :"+ Utilities.getFormattedTime(System.currentTimeMillis()));
            Chat chat = new Chat(contactJid,"",Chat.ContactType.ONE_ON_ONE, System.currentTimeMillis(),0);
            ChatModel.get(getApplicationContext()).addChat(chat);
            Intent intent = new Intent(ContactListActivity.this,ChatViewActivity.class);
            intent.putExtra("contact_jid",contactJid);
            startActivity(intent);
            finish();
        }else
        {
            Log.d(LOGTAG, contactJid + " Chat existiert");
            Intent intent = new Intent(ContactListActivity.this,ChatViewActivity.class);
            intent.putExtra("contact_jid",contactJid);
            startActivity(intent);
            finish();
        }
    }
    @Override
    public void onItemLongClick(final int uniqueId, final String contactJid, View anchor) {
        PopupMenu popup = new PopupMenu(ContactListActivity.this,anchor, Gravity.CENTER);
        popup.getMenuInflater().inflate(R.menu.contact_list_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch( item.getItemId())
                {
                    case R.id.delete_contact :
                        if(ContactModel.get(getApplicationContext()).deleteContact(uniqueId) )
                        {
                            mAdapter.onContactCountChange();
                            if(RoosterConnectionService.getConnection().removeRosterEntry(contactJid))
                            {
                                Log.d(LOGTAG,contactJid + "Successfully deleted from Roster");
                                Toast.makeText(
                                        ContactListActivity.this,
                                        "Contact deleted successfully ",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                        break;
                    case R.id.contact_details:
                        Intent i = new Intent(ContactListActivity.this,ContactDetailsActivity.class);
                        i.putExtra("contact_jid",contactJid);
                        startActivity(i);
                        return true;
                }
                return true;
            }
        });
        popup.show();
    }
}