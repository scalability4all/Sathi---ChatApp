package com.scalability4all.sathi.persistence;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.scalability4all.sathi.model.Contact;

public class ContactCursorWrapper extends CursorWrapper {
    public ContactCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Contact getContact() {
        String subscriptionTypeString = getString(getColumnIndex(Contact.Cols.SUBSCRIPTION_TYPE));
        String jid = getString(getColumnIndex(Contact.Cols.CONTACT_JID));
        int contactUniqueId = getInt(getColumnIndex(Contact.Cols.CONTACT_UNIQUE_ID));
        String profileImagePath = getString(getColumnIndex(Contact.Cols.PROFILE_IMAGE_PATH));
        int pendingFromInt = getInt(getColumnIndex(Contact.Cols.PENDING_STATUS_FROM));
        int pendingToInt = getInt(getColumnIndex(Contact.Cols.PENDING_STATUS_TO));
        int onlineStatusInt = getInt(getColumnIndex(Contact.Cols.ONLINE_STATUS));
        Contact.SubscriptionType subscriptionType = null;
        if (subscriptionTypeString.equals("NONE")) {
            subscriptionType = Contact.SubscriptionType.NONE;
        } else if (subscriptionTypeString.equals("FROM")) {
            subscriptionType = Contact.SubscriptionType.FROM;
        } else if (subscriptionTypeString.equals("TO")) {
            subscriptionType = Contact.SubscriptionType.TO;
        } else if (subscriptionTypeString.equals("BOTH")) {
            subscriptionType = Contact.SubscriptionType.BOTH;
        }
        Contact contact = new Contact(jid, subscriptionType);
        contact.setPersistID(contactUniqueId);
        contact.setProfileImagePath(profileImagePath);
        contact.setPendingFrom(pendingFromInt != 0);
        contact.setPendingTo(pendingToInt != 0);
        contact.setOnlineStatus(onlineStatusInt != 0);
        return contact;
    }
}