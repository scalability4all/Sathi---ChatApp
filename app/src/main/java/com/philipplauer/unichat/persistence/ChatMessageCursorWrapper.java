package com.philipplauer.unichat.persistence;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.philipplauer.unichat.model.ChatMessage;

public class ChatMessageCursorWrapper extends CursorWrapper {

    public ChatMessageCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public ChatMessage getChatMessage()
    {
        String message = getString(getColumnIndex(ChatMessage.Cols.MESSAGE));
        long timestamp = getLong(getColumnIndex(ChatMessage.Cols.TIMESTAMP));
        String messageType = getString(getColumnIndex(ChatMessage.Cols.MESSAGE_TYPE));
        String counterpartJid = getString(getColumnIndex(ChatMessage.Cols.CONTACT_JID));
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
        ChatMessage chatMessage = new ChatMessage(message,timestamp,chatMessageType,counterpartJid);
        chatMessage.setPersistID(uniqueId);
        return  chatMessage;
    }
}
