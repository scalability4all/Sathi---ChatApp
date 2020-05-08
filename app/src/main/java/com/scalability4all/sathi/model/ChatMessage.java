package com.scalability4all.sathi.model;

import android.content.ContentValues;

public class ChatMessage {
    private String message;
    private long timestamp;
    private Type type;
    private String fromContactJid;

    private String toContactJid;
    private int persistID;
    public static final String TABLE_NAME = "chatMessages";

    public static final class Cols {
        public static final String CHAT_MESSAGE_UNIQUE_ID = "chatMessageUniqueId";
        public static final String MESSAGE = "message";
        public static final String TIMESTAMP = "timestamp";
        public static final String MESSAGE_TYPE = "messageType";
        public static final String FROM_CONTACT_JID = "fromContactjid";
        public static final String TO_CONTACT_JID = "toContactjid"; // to here represents before version of contactJid
    }

    public ChatMessage(String message, long timestamp, Type type, String toContactJid, String fromContactJid) {
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.fromContactJid = fromContactJid;
        this.toContactJid = toContactJid;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getPersistID() {
        return persistID;
    }

    public void setPersistID(int persistID) {
        this.persistID = persistID;
    }

    public String getFromContactJid() {
        return fromContactJid;
    }

    public void setFromContactJid(String fromContactJid) {
        this.fromContactJid = fromContactJid;
    }

    public String getToContactJid() {
        return toContactJid;
    }

    public void setToContactJid(String toContactJid) {
        this.toContactJid = toContactJid;
    }

    public String getTypeStringValue(Type type) {
        if (type == Type.SENT)
            return "SENT";
        else
            return "RECEIVED";
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(Cols.MESSAGE, message);
        values.put(Cols.TIMESTAMP, timestamp);
        values.put(Cols.MESSAGE_TYPE, getTypeStringValue(type));
        values.put(Cols.FROM_CONTACT_JID, fromContactJid);
        values.put(Cols.TO_CONTACT_JID, toContactJid);
        return values;
    }

    public enum Type {
        SENT, RECEIVED
    }
}