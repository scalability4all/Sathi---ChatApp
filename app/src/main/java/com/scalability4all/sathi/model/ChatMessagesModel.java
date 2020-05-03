package com.scalability4all.sathi.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.scalability4all.sathi.persistence.ChatMessageCursorWrapper;
import com.scalability4all.sathi.persistence.DatabaseBackend;
import java.util.ArrayList;
import java.util.List;

public class ChatMessagesModel {
    private static final String LOGTAG = "ChatMessagesModel";
    private static ChatMessagesModel sChatMessagesModel;
    private SQLiteDatabase mDatabase;
    private Context context;
    List<ChatMessage> messages;
    public static ChatMessagesModel get(Context context)
    {
        if( sChatMessagesModel == null)
        {
            sChatMessagesModel = new ChatMessagesModel(context);
        }
        return sChatMessagesModel;
    }
    private ChatMessagesModel(Context context)
    {
        this.context = context;
        mDatabase = DatabaseBackend.getInstance(context).getWritableDatabase();
    }
    public List<ChatMessage> getMessages(String counterpartJid,String fromJid)
    {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessageCursorWrapper cursor = queryMessages("(toContactJid= ? and fromContactJid= ?) or (fromContactJid= ? and toContactJid= ?)",new String[] {counterpartJid,fromJid,fromJid,counterpartJid});
        try
        {
            cursor.moveToFirst();
            while( !cursor.isAfterLast())
            {
                messages.add(cursor.getChatMessage());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return messages;
    }
    public boolean addMessage(ChatMessage message)
    {
        ContentValues values = message.getContentValues();
        if ((mDatabase.insert(ChatMessage.TABLE_NAME, null, values)== -1))
        {
            return false;
        }else
        {
            ChatModel.get(context).updateLastMessageDetails(message);
            return true;
        }
    }
    public boolean deleteMessage(ChatMessage message)
    {
        return  deleteMessage(message.getPersistID());
    }
    public boolean  deleteMessage( int uniqueId)
    {
        int value =mDatabase.delete(ChatMessage.TABLE_NAME,ChatMessage.Cols.CHAT_MESSAGE_UNIQUE_ID+"=?",new String[] {String.valueOf(uniqueId)});
        if(value == 1)
        {
            Log.d(LOGTAG, "Successfully deleted a record");
            return true;
        }else
        {
            Log.d(LOGTAG, "Could not delete record");
            return false;
        }
    }
    private ChatMessageCursorWrapper queryMessages(String whereClause , String[] whereArgs)
    {
        Cursor cursor = mDatabase.query(ChatMessage.TABLE_NAME, null , whereClause, whereArgs, null , null, null);
        return new ChatMessageCursorWrapper(cursor);
    }
}