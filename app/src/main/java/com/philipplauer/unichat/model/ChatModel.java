package com.philipplauer.unichat.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.philipplauer.unichat.persistence.ChatCursorWrapper;
import com.philipplauer.unichat.persistence.DatabaseBackend;
import java.util.ArrayList;
import java.util.List;

public class ChatModel {
    private static final String LOGTAG = "ChatModel" ;
    private static ChatModel sChatsModel;
    private Context mContext;
    private SQLiteDatabase mDatabase;
    public static ChatModel get(Context context)
    {
        if(sChatsModel == null)
        {
            sChatsModel = new ChatModel(context);
        }
        return sChatsModel;
    }
    private ChatModel(Context context)
    {
        mContext = context;
        mDatabase = DatabaseBackend.getInstance(mContext).getWritableDatabase();
    }
    public List<Chat> getChats()
    {
        List<Chat> chats = new ArrayList<>();
        ChatCursorWrapper cursor = queryChats(null,null);
        try
        {
            cursor.moveToFirst();
            while( !cursor.isAfterLast())
            {
                Log.d(LOGTAG , "Chat aus DB : Timestamp :"+cursor.getChat().getLastMessageTimeStamp());
                chats.add(cursor.getChat());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return chats;
    }
    public List<Chat> getChatsByJid(String jid)
    {
        List<Chat> chats = new ArrayList<>();
        ChatCursorWrapper cursor = queryChats(Chat.Cols.CONTACT_JID + "= ?",new String[] {jid});
        try
        {
            cursor.moveToFirst();
            while( !cursor.isAfterLast())
            {
                chats.add(cursor.getChat());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return chats;
    }
    public boolean addChat( Chat c )
    {
        ContentValues values = c.getContentValues();
        if ((mDatabase.insert(Chat.TABLE_NAME, null, values)== -1))
        {
            return false;
        }else
        {
            return true;
        }
    }
    public boolean updateLastMessageDetails(ChatMessage chatMessage)
    {
        List<Chat> chats = getChatsByJid(chatMessage.getContactJid());
        if( !chats.isEmpty())
        {
            Chat chat = chats.get(0);
            chat.setLastMessageTimeStamp(chatMessage.getTimestamp());
            chat.setLastMessage(chatMessage.getMessage());
            ContentValues values = chat.getContentValues();
            int ret =mDatabase.update(Chat.TABLE_NAME, values,
                    Chat.Cols.CHAT_UNIQUE_ID + "=?",
                    new String[]{ String.valueOf(chat.getPersistID())});
            if(ret == 1)
            {
                return true;
            }else
            {
                return false;
            }
        }
        return false;
    }
    public boolean deleteChat(Chat c)
    {
        return deleteChat(c.getPersistID());
    }
    public boolean deleteChat(int uniqueId)
    {
        int value =mDatabase.delete(Chat.TABLE_NAME,Chat.Cols.CHAT_UNIQUE_ID+"=?",new String[] {String.valueOf(uniqueId)});
        if(value == 1)
        {
            return true;
        }else
        {
            return false;
        }
    }
    private ChatCursorWrapper queryChats(String whereClause , String[] whereArgs)
    {
        Cursor cursor = mDatabase.query(Chat.TABLE_NAME, null , whereClause, whereArgs, null , null, null);
        return new ChatCursorWrapper(cursor);
    }
}