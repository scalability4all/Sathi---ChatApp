package com.philipplauer.unichat.persistence;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import com.philipplauer.unichat.model.Chat;

public class ChatCursorWrapper extends CursorWrapper {
    private static final String LOGTAG = "ChatCursorWrapper" ;

    public ChatCursorWrapper(Cursor cursor) {
        super(cursor);
    }
    public Chat getChat() {
        String jid = getString(getColumnIndex(Chat.Cols.CONTACT_JID));
        String contactType = getString(getColumnIndex(Chat.Cols.CONTACT_TYPE));
        String lastMessage = getString(getColumnIndex(Chat.Cols.LAST_MESSAGE));
        long unreadCount = getLong(getColumnIndex(Chat.Cols.UNREAD_COUNT));
        long lastMessageTimeStamp = getLong(getColumnIndex(Chat.Cols.LAST_MESSAGE_TIME_STAMP));
        int uniqueId = getInt(getColumnIndex(Chat.Cols.CHAT_UNIQUE_ID));
        Log.d(LOGTAG,"Chat aus Datenbank - ID: "+uniqueId);
        Chat.ContactType chatType = null;
        if (contactType.equals("GROUP")) {
            chatType = Chat.ContactType.GROUP;
        } else if (contactType.equals("ONE_ON_ONE")) {
            chatType = Chat.ContactType.ONE_ON_ONE;
        }else if(contactType.equals("STRANGER"))
        {
            chatType = Chat.ContactType.STRANGER;
        }
        Chat chat = new Chat(jid,lastMessage,chatType, lastMessageTimeStamp,unreadCount);
        chat.setPersistID(uniqueId);
        return chat;
    }
}