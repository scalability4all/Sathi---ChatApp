package com.philipplauer.unichat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.philipplauer.unichat.model.Contact;
import com.philipplauer.unichat.model.ContactModel;
import com.philipplauer.unichat.xmpp.RoosterConnection;
import com.philipplauer.unichat.xmpp.RoosterConnectionService;

public class ContactDetailsActivity extends AppCompatActivity {
    private static final String LOGTAG = "ContactDetailsActivity" ;
    private String contactJid;
    private CheckBox fromCheckBox;
    private CheckBox toCheckBox;
    private Context mApplicationContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        mApplicationContext=getApplicationContext();
        Intent intent = getIntent();
        contactJid = intent.getStringExtra("contact_jid");
        setTitle(contactJid);
        ImageView profileImage = findViewById(R.id.contact_details_user_profile);
        RoosterConnection rc = RoosterConnectionService.getConnection();
        profileImage.setImageResource(R.drawable.ic_profile);
        if(rc != null)
        {
            String imageAbsPath = rc.getProfileImageAbsolutePath(contactJid);
            if ( imageAbsPath != null)
            {
                Drawable d = Drawable.createFromPath(imageAbsPath);
                profileImage.setImageDrawable(d);
            }
        }
        TextView pendingFrom = findViewById(R.id.pending_from);
        TextView pendingTo = findViewById(R.id.pending_to);
        fromCheckBox = findViewById(R.id.them_to_me);
        fromCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( fromCheckBox.isChecked())
                {
                    Log.d(LOGTAG,"FROM Checkbox checked");
                }else
                {
                    Log.d(LOGTAG,"FROM Checkbox unchecked");
                    if(RoosterConnectionService.getConnection().unsubscribed(contactJid))
                    {
                        Toast.makeText(mApplicationContext,"Onlinestatus wird nicht mehr übertragen: "+ contactJid, Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
        toCheckBox = findViewById(R.id.me_to_tem);
        toCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( toCheckBox.isChecked())
                {
                    Log.d(LOGTAG,"Subscribtion checkbox checked");
                    if(RoosterConnectionService.getConnection().subscribe(contactJid))
                    {
                        Toast.makeText(mApplicationContext,"Abo-Anfrage wurde gesendet:  "+ contactJid, Toast.LENGTH_LONG).show();
                    }
                }else
                {
                    Log.d(LOGTAG,"Subscribe checkbox unchecked");
                    if(RoosterConnectionService.getConnection().unsubscribe(contactJid))
                    {
                        Toast.makeText(mApplicationContext,"Onlinestatus wird nicht mehr empfangen:  "+ contactJid, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        // Aboabfrage Kontakt und entsprechende Layoutänderungen
        if(!ContactModel.get(getApplication()).isContactStranger(contactJid))
        {
            Contact contact = ContactModel.get(getApplicationContext()).getContactByJidString(contactJid);
            Contact.SubscriptionType subType = contact.getSubscriptionType();
            if(subType == Contact.SubscriptionType.NONE)
            {
                fromCheckBox.setEnabled(false);
                fromCheckBox.setChecked(false);
                toCheckBox.setChecked(false);
            }else if (subType == Contact.SubscriptionType.FROM)
            {
                fromCheckBox.setEnabled(true);
                fromCheckBox.setChecked(true);
                toCheckBox.setChecked(false);
            }else if (subType == Contact.SubscriptionType.TO)
            {
                fromCheckBox.setEnabled(false);
                fromCheckBox.setChecked(false);
                toCheckBox.setChecked(true);
            }else if (subType == Contact.SubscriptionType.BOTH)
            {
                fromCheckBox.setEnabled(true);
                fromCheckBox.setChecked(true);
                toCheckBox.setChecked(true);
            }
            if(contact.isPendingFrom())
            {
                pendingFrom.setVisibility(View.VISIBLE);
            }else
            {
                pendingFrom.setVisibility(View.GONE);
            }
            if(contact.isPendingTo())
            {
                pendingTo.setVisibility(View.VISIBLE);
            }else
            {
                pendingTo.setVisibility(View.GONE);
            }
            if(subType == Contact.SubscriptionType.NONE)
            {
                fromCheckBox.setEnabled(false);
                fromCheckBox.setChecked(false);
                toCheckBox.setChecked(false);
            }else if (subType == Contact.SubscriptionType.FROM)
            {
                fromCheckBox.setEnabled(true);
                fromCheckBox.setChecked(true);
                toCheckBox.setChecked(false);
            }else if (subType == Contact.SubscriptionType.TO)
            {
                fromCheckBox.setEnabled(false);
                fromCheckBox.setChecked(false);
                toCheckBox.setChecked(true);
            }else if (subType == Contact.SubscriptionType.BOTH)
            {
                fromCheckBox.setEnabled(true);
                fromCheckBox.setChecked(true);
                toCheckBox.setChecked(true);
            }
        }else
        {
            fromCheckBox.setEnabled(false);
            fromCheckBox.setChecked(false);
            toCheckBox.setChecked(false);
            toCheckBox.setEnabled(true);
        }
    }
}