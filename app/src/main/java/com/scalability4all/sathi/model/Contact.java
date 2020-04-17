package com.scalability4all.sathi.model;

import android.content.ContentValues;

public class Contact {
    private String jid;
    private SubscriptionType subscriptionType;
    private String profileImagePath;
    private int persistID;
    boolean  pendingTo;
    boolean pendingFrom;
    boolean onlineStatus;

    public static final String TABLE_NAME = "contacts";
    public static final class Cols
    {
        public static final String CONTACT_UNIQUE_ID = "contactUniqueId";
        public static final String CONTACT_JID = "jid";
        public static final String SUBSCRIPTION_TYPE = "subscriptionType";
        public static final String PROFILE_IMAGE_PATH = "profileImagePath";
        public static final String PENDING_STATUS_TO = "pendingTo";
        public static final String PENDING_STATUS_FROM = "pendingFrom";
        public static final String ONLINE_STATUS = "onlineStatus";
    }
    public ContentValues getContentValues()
    {
        ContentValues values = new ContentValues();
        int pendingFromInt = (pendingFrom)? 1 : 0;
        int pendingToInt = (pendingTo)? 1 : 0;
        int onlineStatusInt = (onlineStatus)? 1 : 0;
        values.put(Cols.CONTACT_JID, jid);
        values.put(Cols.SUBSCRIPTION_TYPE,getTypeStringValue(subscriptionType));
        values.put(Cols.PROFILE_IMAGE_PATH,profileImagePath);
        values.put(Cols.PENDING_STATUS_FROM, pendingFromInt);
        values.put(Cols.PENDING_STATUS_TO,pendingToInt);
        values.put(Cols.ONLINE_STATUS,onlineStatusInt);
        return values;
    }
    public String getTypeStringValue(SubscriptionType type)
    {
        if(type== SubscriptionType.FROM)
            return "FROM";
        else if(type == SubscriptionType.TO)
            return "TO";
        else if(type == SubscriptionType.BOTH)
            return "BOTH";
        else if(type == SubscriptionType.NONE)
            return "NONE";
        else
            return "INDETERMINATE";
    }
    public enum SubscriptionType{
        NONE,FROM,TO,BOTH
    }
    public Contact(String jid, SubscriptionType subscriptionType) {
        this.jid = jid;
        this.subscriptionType = subscriptionType;
        this.profileImagePath = "NONE";
        this.pendingFrom = false;
        this.pendingTo = false;
        this.onlineStatus = false;
    }
    public String getJid() {
        return jid;
    }
    public void setJid(String jid) {
        this.jid = jid;
    }
    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }
    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
    }
    public String getProfileImagePath() {
        return profileImagePath;
    }
    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
    public int getPersistID() {
        return persistID;
    }
    public void setPersistID(int persistID) {
        this.persistID = persistID;
    }
    public boolean isPendingTo() {
        return pendingTo;
    }
    public void setPendingTo(boolean pendingTo) {
        this.pendingTo = pendingTo;
    }
    public boolean isPendingFrom() {
        return pendingFrom;
    }
    public void setPendingFrom(boolean pendingFrom) {
        this.pendingFrom = pendingFrom;
    }
    public boolean isOnlineStatus() {
        return onlineStatus;
    }
    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
}