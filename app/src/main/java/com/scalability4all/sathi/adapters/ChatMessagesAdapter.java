package com.scalability4all.sathi.adapters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.scalability4all.sathi.BuildConfig;
import com.scalability4all.sathi.ChatViewActivity;
import com.scalability4all.sathi.FileHandler.FileDownloader;
import com.scalability4all.sathi.FileHandler.Helpers;
import com.scalability4all.sathi.FileHandler.MainActivity;
import com.scalability4all.sathi.R;
import com.scalability4all.sathi.Utilities;
import com.scalability4all.sathi.model.ChatMessage;
import com.scalability4all.sathi.model.ChatMessagesModel;
import com.scalability4all.sathi.xmpp.RoosterConnection;
import com.scalability4all.sathi.xmpp.RoosterConnectionService;

import java.io.File;
import java.util.List;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {
    public interface OnInformRecyclerViewToScrollDownListener {
        void onInformRecyclerViewToScrollDown(int size);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int uniqueId, View anchor);
    }

    private static final int SENT = 1;
    private static final int RECEIVED = 2;
    private static final String LOGTAG = "ChatMessageAdapter";
    private List<ChatMessage> mChatMessageList;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private String toContactJid;
    public OnDownloadListener listener;

    public String getFromContactJid() {
        return fromContactJid;
    }

    public void setFromContactJid(String fromContactJid) {
        this.fromContactJid = fromContactJid;
    }

    private String fromContactJid;
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
    public void setListener(OnDownloadListener listener){
        this.listener = listener;
    }
    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public ChatMessagesAdapter(Context context, String toContactJid, String fromContactJid) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.toContactJid = toContactJid;
        this.fromContactJid = fromContactJid;
        mChatMessageList = ChatMessagesModel.get(context).getMessages(toContactJid, fromContactJid);
        Log.d(LOGTAG, "Getting messages for :" + toContactJid);
    }

    public void informRecyclerViewToScrollDown() {
        mOnInformRecyclerViewToScrollDownListener.onInformRecyclerViewToScrollDown(mChatMessageList.size());
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case SENT:
                itemView = mLayoutInflater.inflate(R.layout.chat_message_sent, parent, false);
                return new ChatMessageViewHolder(itemView, this);
            case RECEIVED:
                itemView = mLayoutInflater.inflate(R.layout.chat_message_received, parent, false);
                return new ChatMessageViewHolder(itemView, this);
            default:
                itemView = mLayoutInflater.inflate(R.layout.chat_message_sent, parent, false);
                return new ChatMessageViewHolder(itemView, this);
        }
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        ChatMessage chatMessage = mChatMessageList.get(position);
        holder.bindChat(chatMessage);
    }

    @Override
    public int getItemCount() {
        return mChatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage.Type messageType = mChatMessageList.get(position).getType();
        if (messageType == ChatMessage.Type.SENT) {
            return SENT;
        } else {
            return RECEIVED;
        }
    }

    public void onMessageAdd() {
        mChatMessageList = ChatMessagesModel.get(mContext).getMessages(toContactJid, fromContactJid);
        notifyDataSetChanged();
        informRecyclerViewToScrollDown();
    }
    public interface OnDownloadListener{
        void onDownloadListener(ChatMessage message);
    }
}

class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    private static final String LOGTAG = "ChatMessageViewHolder";
    private TextView mMessageBody, mMessageTimestamp;
    private ImageView profileImage;
    private ChatMessage mChatMessage;
    private ChatMessagesAdapter mAdapter;


    public ChatMessageViewHolder(final View itemView, final ChatMessagesAdapter mAdapter) {
        super(itemView);

        mMessageBody = itemView.findViewById(R.id.text_message_body);

        mMessageTimestamp = itemView.findViewById(R.id.text_message_timestamp);
        profileImage = itemView.findViewById(R.id.profile);
        this.mAdapter = mAdapter;

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ChatMessagesAdapter.OnItemLongClickListener listener = mAdapter.getOnItemLongClickListener();
                if (listener != null) {
                    listener.onItemLongClick(mChatMessage.getPersistID(), itemView);
                }
                return false;
            }
        });
    }


    public void bindChat(ChatMessage chatMessage) {
        mChatMessage = chatMessage;
        String message = chatMessage.getMessage();
        ChatMessage.Type type = mChatMessage.getType();
        mMessageTimestamp.setText(Utilities.getFormattedTime(chatMessage.getTimestamp()));
        profileImage.setImageResource(R.mipmap.ic_profile);
        Helpers help = new Helpers();
        if (type == ChatMessage.Type.RECEIVED) {
            if(help.IsUploadMessage(message)){
                Bitmap icon = BitmapFactory.decodeResource(mAdapter.getContext().getResources(),
                        R.mipmap.download_file);
                help.setImage(icon, mMessageBody);
                mMessageBody.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.listener.onDownloadListener(mChatMessage);
                    }
                });
            }
            else if(help.IsDownloadmessage(message)){
                String filename = help.GetDownloadFileName(message);
                File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                Bitmap bm = help.getBitmap(destinationFile);
                help.setImage(bm, mMessageBody);
                mMessageBody.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View V){
                        Intent myIntent = new Intent(Intent.ACTION_VIEW);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = FileProvider.getUriForFile(mAdapter.getContext(), BuildConfig.APPLICATION_ID + ".provider", destinationFile);
                        myIntent.setData(uri);
                        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                        j.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        mAdapter.getContext().startActivity(j);
                    }
                });
            }
            else {
                mMessageBody.setText(message);
                // https://github.com/saket/Better-Link-Movement-Method
                // http://saket.me/better-url-handler-textview-android/
                BetterLinkMovementMethod
                        .linkify(Linkify.ALL, mMessageBody)
                        .setOnLinkClickListener((textView, url) -> {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.setData(Uri.parse(url));
                            mAdapter.getContext().startActivity(i);
                            return true;
                        })
                        .setOnLinkLongClickListener((textView, url) -> {
                            // Handle long-clicks.
                            return true;
                        });
            }
            RoosterConnection rc = RoosterConnectionService.getConnection();
            if (rc != null) {
                String imageAbsPath = rc.getProfileImageAbsolutePath(mChatMessage.getToContactJid());
                if (imageAbsPath != null) {
                    Drawable d = Drawable.createFromPath(imageAbsPath);
                    profileImage.setImageDrawable(d);
                }
            }
        }
        if (type == ChatMessage.Type.SENT) {
            if(help.IsUploadMessage(message)){
                String file = help.GetUriFromUpload(message);
                Bitmap bm = help.getBitmap(file);
                help.setImage(bm, mMessageBody);
                mMessageBody.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View V){
                        Intent myIntent = new Intent(Intent.ACTION_VIEW);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = FileProvider.getUriForFile(mAdapter.getContext(), BuildConfig.APPLICATION_ID + ".provider",new File(file));
                        myIntent.setData(uri);
                        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                        j.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mAdapter.getContext().startActivity(j);
                    }
                });
            }
            else {
                mMessageBody.setText(message);
                // https://github.com/saket/Better-Link-Movement-Method
                // http://saket.me/better-url-handler-textview-android/
                BetterLinkMovementMethod
                        .linkify(Linkify.ALL, mMessageBody)
                        .setOnLinkClickListener((textView, url) -> {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.setData(Uri.parse(url));
                            mAdapter.getContext().startActivity(i);
                            return true;
                        })
                        .setOnLinkLongClickListener((textView, url) -> {
                            // Handle long-clicks.
                            return true;
                        });
            }
            RoosterConnection rc = RoosterConnectionService.getConnection();
            if (rc != null) {
                String selfJid = PreferenceManager.getDefaultSharedPreferences(mAdapter.getContext()).getString("xmpp_jid", null);
                if (selfJid != null) {
                    Log.d(LOGTAG, "Valide SID : " + selfJid);
                    String imageAbsPath = rc.getProfileImageAbsolutePath(selfJid);
                    if (imageAbsPath != null) {
                        Drawable d = Drawable.createFromPath(imageAbsPath);
                        profileImage.setImageDrawable(d);
                    }
                } else {
                    Log.d(LOGTAG, "Keine valide SID ");
                }
            }
        }
    }

}