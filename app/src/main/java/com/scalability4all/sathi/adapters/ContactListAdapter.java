package com.scalability4all.sathi.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.scalability4all.sathi.R;
import com.scalability4all.sathi.model.Contact;
import com.scalability4all.sathi.model.ContactModel;
import com.scalability4all.sathi.xmpp.RoosterConnection;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;
import java.util.List;

public class ContactListAdapter extends RecyclerView.Adapter<ContactHolder> {
    public interface OnItemClickListener {
        public void onItemClick(String contactJid);
    }
    public interface OnItemLongClickListener{
        public void onItemLongClick(int uniqueId, String contactJid, View anchor);
    }
    private List<Contact> mContacts;
    private Context mContext;
    private static final String LOGTAG = "ContactListAdapter";
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    public ContactListAdapter(Context context)
    {
        mContacts = ContactModel.get(context).getContacts();
        mContext = context;
    }
    public OnItemClickListener getmOnItemClickListener() {
        return mOnItemClickListener;
    }
    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
    public OnItemLongClickListener getmOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }
    public void setmOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }
    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.contact_list_item, parent, false);
        return new ContactHolder(view,this);
    }
    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        Contact contact = mContacts.get(position);
        holder.bindContact(contact);
    }
    @Override
    public int getItemCount() {
        return mContacts.size();
    }
    public void onContactCountChange() {
        mContacts = ContactModel.get(mContext).getContacts();
        notifyDataSetChanged();
    }
}
class ContactHolder extends RecyclerView.ViewHolder {
    private TextView jidTexView;
    private TextView subscriptionTypeTextView;
    private Contact mContact;
    private ImageView profile_image;
    private ContactListAdapter mAdapter;
    private static final String LOGTAG = "ContactHolder";
    public ContactHolder(final View itemView , ContactListAdapter adapter) {
        super(itemView);
        mAdapter = adapter;
        jidTexView = itemView.findViewById(R.id.contact_jid_string);
        subscriptionTypeTextView = itemView.findViewById(R.id.suscription_type);
        profile_image = itemView.findViewById(R.id.profile_contact);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOGTAG,"User Klick auf Kontaktitem");
                ContactListAdapter.OnItemClickListener listener = mAdapter.getmOnItemClickListener();
                if ( listener != null)
                {
                    listener.onItemClick(jidTexView.getText().toString());
                }
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ContactListAdapter.OnItemLongClickListener listener = mAdapter.getmOnItemLongClickListener();
                if ( listener != null)
                {
                    mAdapter.getmOnItemLongClickListener().onItemLongClick(mContact.getPersistID(),mContact.getJid(),itemView);
                    return true;
                }
                return false;
            }
        });
    }
    void bindContact(Contact c)
    {
        mContact = c;
        if ( mContact == null)
        {
            return;
        }
        jidTexView.setText(mContact.getJid());
        subscriptionTypeTextView.setText(mContact.getTypeStringValue(mContact.getSubscriptionType()));
        profile_image.setImageResource(R.mipmap.ic_profile);
        RoosterConnection rc = RoosterConnectionService.getConnection();
        if(rc != null)
        {
            String imageAbsPath = rc.getProfileImageAbsolutePath(mContact.getJid());
            if ( imageAbsPath != null)
            {
                Drawable d = Drawable.createFromPath(imageAbsPath);
                profile_image.setImageDrawable(d);
            }
        }
    }
}