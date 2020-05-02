package com.scalability4all.sathi.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.scalability4all.sathi.R;
import com.scalability4all.sathi.Utilities;
import com.scalability4all.sathi.model.ChatMessage;
import com.scalability4all.sathi.model.ChatMessagesModel;
import com.scalability4all.sathi.xmpp.RoosterConnection;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;
import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {
    public interface OnInformRecyclerViewToScrollDownListener {
        public void onInformRecyclerViewToScrollDown(int size);
    }
    public interface OnItemLongClickListener{
        public void onItemLongClick(int uniqueId, View anchor);
    }
    private static final int SENT = 1;
    private static final int RECEIVED = 2;
    private static final String LOGTAG ="ChatMessageAdapter" ;
    private List<ChatMessage> mChatMessageList;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private String contactJid;
    private OnInformRecyclerViewToScrollDownListener mOnInformRecyclerViewToScrollDownListener;
    private OnItemLongClickListener onItemLongClickListener;
    public void setmOnInformRecyclerViewToScrollDownListener(OnInformRecyclerViewToScrollDownListener mOnInformRecyclerViewToScrollDownListener) {
        this.mOnInformRecyclerViewToScrollDownListener = mOnInformRecyclerViewToScrollDownListener;
    }
    public OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }
    public Context getContext() {
        return mContext;
    }
    public void setContext(Context mContext) {
        this.mContext = mContext;
    }
    public ChatMessagesAdapter(Context context, String contactJid)
    {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.contactJid = contactJid;
        mChatMessageList = ChatMessagesModel.get(context).getMessages(contactJid);
        Log.d(LOGTAG,"Getting messages for :"+ contactJid);
    }
    public void informRecyclerViewToScrollDown()
    {
        mOnInformRecyclerViewToScrollDownListener.onInformRecyclerViewToScrollDown(mChatMessageList.size());
    }
    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType)
        {
            case  SENT :
                itemView = mLayoutInflater.inflate(R.layout.chat_message_sent,parent,false);
                return new ChatMessageViewHolder(itemView,this);
            case RECEIVED:
                itemView = mLayoutInflater.inflate(R.layout.chat_message_received,parent,false);
                return new ChatMessageViewHolder(itemView,this);
            default:
                itemView = mLayoutInflater.inflate(R.layout.chat_message_sent,parent,false);
                return new ChatMessageViewHolder(itemView,this);
        }
    }
    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        ChatMessage chatMessage =mChatMessageList.get(position);
        holder.bindChat(chatMessage);
    }
    @Override
    public int getItemCount() {
        return mChatMessageList.size();
    }
    @Override
    public int getItemViewType(int position) {
        ChatMessage.Type messageType = mChatMessageList.get(position).getType();
        if ( messageType == ChatMessage.Type.SENT)
        {
            return SENT;
        }else{
            return RECEIVED;
        }
    }
    public void onMessageAdd() {
        mChatMessageList = ChatMessagesModel.get(mContext).getMessages(contactJid);
        notifyDataSetChanged();
        informRecyclerViewToScrollDown();
    }
}
class ChatMessageViewHolder extends RecyclerView.ViewHolder{
    private static final String LOGTAG = "ChatMessageViewHolder" ;
    private TextView mMessageBody, mMessageTimestamp;
    private ImageView profileImage;
    private ChatMessage mChatMessage;
    private ChatMessagesAdapter mAdapter;
    public ChatMessageViewHolder(final View itemView, final ChatMessagesAdapter mAdapter) {
        super(itemView);
        mMessageBody = itemView.findViewById(R.id.text_message_body);
        mMessageBody.setMovementMethod(LinkMovementMethod.getInstance());
        mMessageTimestamp = itemView.findViewById(R.id.text_message_timestamp);
        profileImage = itemView.findViewById(R.id.profile);
        this.mAdapter = mAdapter;
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ChatMessagesAdapter.OnItemLongClickListener listener = mAdapter.getOnItemLongClickListener();
                if ( listener!=null)
                {
                    listener.onItemLongClick(mChatMessage.getPersistID(),itemView);
                }
                return false;
            }
        });
    }
    public void bindChat(ChatMessage chatMessage)
    {
        mChatMessage = chatMessage;
        mMessageBody.setText(chatMessage.getMessage());
        mMessageTimestamp.setText(Utilities.getFormattedTime(chatMessage.getTimestamp()));
        profileImage.setImageResource(R.drawable.ic_profile);
        ChatMessage.Type type = mChatMessage.getType();
        if( type == ChatMessage.Type.RECEIVED)
        {
            RoosterConnection rc = RoosterConnectionService.getConnection();
            if(rc != null)
            {
                String imageAbsPath = rc.getProfileImageAbsolutePath(mChatMessage.getContactJid());
                if ( imageAbsPath != null)
                {
                    Drawable d = Drawable.createFromPath(imageAbsPath);
                    profileImage.setImageDrawable(d);
                }
            }
        }
        if( type == ChatMessage.Type.SENT)
        {
            RoosterConnection rc = RoosterConnectionService.getConnection();
            if(rc != null)
            {
                String selfJid = PreferenceManager.getDefaultSharedPreferences(mAdapter.getContext()).getString("xmpp_jid",null);
                if ( selfJid != null)
                {
                    Log.d(LOGTAG,"Valide SID : "+ selfJid);
                    String imageAbsPath = rc.getProfileImageAbsolutePath(selfJid);
                    if ( imageAbsPath != null)
                    {
                        Drawable d = Drawable.createFromPath(imageAbsPath);
                        profileImage.setImageDrawable(d);
                    }
                }else
                {
                    Log.d(LOGTAG,"Keine valide SID ");
                }
            }
        }
    }
}