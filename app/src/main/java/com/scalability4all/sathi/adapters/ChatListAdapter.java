package com.scalability4all.sathi.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.scalability4all.sathi.R;
import com.scalability4all.sathi.Utilities;
import com.scalability4all.sathi.model.Chat;
import com.scalability4all.sathi.model.ChatModel;
import com.scalability4all.sathi.xmpp.RoosterConnection;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.scalability4all.sathi.Constants.removeHostNameFromJID;

public class ChatListAdapter extends RecyclerView.Adapter<ChatHolder> {
    private static final String LOGTAG ="ChatListAdapter";
    public interface OnItemClickListener {
        public void onItemClick(String contactJid, Chat.ContactType chatType);
    }
    public interface OnItemLongClickListener{
        public void onItemLongClick(String contactJid, int chatUniqueId, View anchor);
    }
    List<Chat> chatList;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private Context mContext;
    public ChatHolder chatholder;
    String selfJid;
    public ChatListAdapter(Context context) {
        selfJid = PreferenceManager.getDefaultSharedPreferences(context).getString("xmpp_jid",null);
        List<Chat> list = ChatModel.get(context).getChats(selfJid);
        Map<String,Chat> chatListMap=new HashMap<String,Chat>();
        for (Chat element : list) {
            if(element.getToContactJid().equals(selfJid)) {
                if(chatListMap.containsKey(element.getFromContactJid())) {
                    Chat data=chatListMap.get(element.getFromContactJid());
                    if(element.getLastMessageTimeStamp()>data.getLastMessageTimeStamp()) {
                        chatListMap.put(element.getFromContactJid(),element);
                    }
                } else{
                    chatListMap.put(element.getFromContactJid(),element);
                }
            } else {
                if(chatListMap.containsKey(element.getToContactJid())) {
                    Chat data=chatListMap.get(element.getToContactJid());
                    if(element.getLastMessageTimeStamp()>data.getLastMessageTimeStamp()) {
                        chatListMap.put(element.getToContactJid(),element);
                    }
                } else{
                    chatListMap.put(element.getToContactJid(),element);
                }
            }
        }
        this.chatList=new ArrayList<>();
        for(Map.Entry<String, Chat> entry: chatListMap.entrySet()) {
            chatList.add(entry.getValue());
        }
        this.mContext = context;
    }



    public OnItemClickListener getmOnItemClickListener() {
        return mOnItemClickListener;
    }
    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
    public OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }
    @Override
    public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.chat_list_item, parent, false);
        chatholder = new ChatHolder(view,this, mContext);
        return chatholder;
    }
    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.bindChat(chat);
    }
    @Override
    public int getItemCount() {
        return chatList.size();
    }
    public void onChatCountChange() {
        chatList = ChatModel.get(mContext).getChats(selfJid);
        notifyDataSetChanged();
    }
    public void OnNewMessage(String user) {
        chatList = ChatModel.get(mContext).getChats(selfJid);
        notifyDataSetChanged();
        Log.d(LOGTAG, "OnNewMessage"+ " "+ user);
        for (Chat chat:
             chatList) {
             if (chat.getToContactJid().equals(user)) {
                 Log.d(LOGTAG, chat.getToContactJid()+ " "+ user);
                chatholder.bindChat(chat);
            }
        }
    }
}
class ChatHolder extends RecyclerView.ViewHolder{
    private static final String LOGTAG = "ChatHolder";
    private TextView contactTextView;
    private TextView messageAbstractTextView;
    private TextView timestampTextView;
    private ImageView profileImage;
    private Chat mChat;
    private ChatListAdapter mChatListAdapter;
    public Context mContext;
    public ChatHolder(final View itemView , ChatListAdapter adapter, Context mContext) {
        super(itemView);
        this.mContext = mContext;
        contactTextView = itemView.findViewById(R.id.contact_jid);
        messageAbstractTextView = itemView.findViewById(R.id.message_abstract);
        timestampTextView = itemView.findViewById(R.id.text_message_timestamp);
        profileImage = itemView.findViewById(R.id.profile);
        mChatListAdapter = adapter;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatListAdapter.OnItemClickListener listener = mChatListAdapter.getmOnItemClickListener();
                if ( listener!= null)
                {
                    listener.onItemClick(addHostName(contactTextView.getText().toString()),mChat.getContactType());
                }
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ChatListAdapter.OnItemLongClickListener listener = mChatListAdapter.getOnItemLongClickListener();
                if(listener != null)
                {
                    listener.onItemLongClick(mChat.getToContactJid(),mChat.getPersistID(),itemView);
                    return true;
                }
                return false;
            }
        });
    }
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if(html == null){
            // return an empty spannable if the html is null
            return new SpannableString("");
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }
    public void bindChat(Chat chat)
    {
        mChat = chat;
        String selfJid = PreferenceManager.getDefaultSharedPreferences(mContext).getString("xmpp_jid",null);
        if(selfJid.equals(chat.getFromContactJid())) {
            contactTextView.setText(removeHostNameFromJID(chat.getToContactJid()));
        } else {
            contactTextView.setText(removeHostNameFromJID(chat.getFromContactJid()));
        }
        Typeface typeface = ResourcesCompat.getFont(this.mContext, R.font.nanosanslight);
        messageAbstractTextView.setTypeface(typeface);
        String message = chat.getLastMessage();
        TextUtils.htmlEncode(message);
        messageAbstractTextView.setText(fromHtml(message));
        timestampTextView.setMovementMethod(LinkMovementMethod.getInstance());
        timestampTextView.setText(Utilities.getFormattedTime(mChat.getLastMessageTimeStamp()));

        profileImage.setImageResource(R.mipmap.ic_profile);
        RoosterConnection rc = RoosterConnectionService.getConnection();
        if(rc != null)
        {
            String imageAbsPath = rc.getProfileImageAbsolutePath(mChat.getToContactJid());
            if ( imageAbsPath != null)
            {
                Drawable d = Drawable.createFromPath(imageAbsPath);
                profileImage.setImageDrawable(d);
            }
        }
    }
    public String addHostName(String jid) {
        return jid+"@localhost";
    }
}