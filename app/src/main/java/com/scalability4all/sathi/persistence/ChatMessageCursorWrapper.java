package com.scalability4all.sathi.persistence;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.scalability4all.sathi.model.ChatMessage;

public class ChatMessageCursorWrapper extends CursorWrapper {

    public ChatMessageCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public ChatMessage getChatMessage()
    {
        String message = getString(getColumnIndex(ChatMessage.Cols.MESSAGE));
        long timestamp = getLong(getColumnIndex(ChatMessage.Cols.TIMESTAMP));
        String messageType = getString(getColumnIndex(ChatMessage.Cols.MESSAGE_TYPE));
        String counterpartJid = getString(getColumnIndex(ChatMessage.Cols.TO_CONTACT_JID));
        String sendersJid = getString(getColumnIndex(ChatMessage.Cols.FROM_CONTACT_JID));
        int uniqueId = getInt(getColumnIndex(ChatMessage.Cols.CHAT_MESSAGE_UNIQUE_ID));
        ChatMessage.Type chatMessageType = null;
        if( messageType.equals("SENT"))
        {
            chatMessageType = ChatMessage.Type.SENT;
        }
        else if(messageType.equals("RECEIVED"))
        {
            chatMessageType = ChatMessage.Type.RECEIVED;
        }
        ChatMessage chatMessage = new ChatMessage(message,timestamp,chatMessageType,counterpartJid,sendersJid);
        chatMessage.setPersistID(uniqueId);
        return  chatMessage;
    }
}
