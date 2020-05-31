package com.scalability4all.sathi.xmpp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import com.scalability4all.sathi.Constants;
import com.scalability4all.sathi.R;
import com.scalability4all.sathi.Utilities;
import com.scalability4all.sathi.model.ChatMessage;
import com.scalability4all.sathi.model.ChatMessagesModel;
import com.scalability4all.sathi.model.ChatModel;
import com.scalability4all.sathi.model.Contact;
import com.scalability4all.sathi.model.ContactModel;
import com.scalability4all.sathi.services.VolleyCallback;
import com.scalability4all.sathi.services.VolleyService;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import static com.scalability4all.sathi.model.Chat.ContactType.STRANGER;
import static com.scalability4all.sathi.services.VolleyService.POST_TRANSLATION_TEXT;

public class RoosterConnection implements ConnectionListener, SubscribeListener, RosterListener {
    private static final String LOGTAG = "RoosterConnection";
    private final Context mApplicationContext;
    private String mUsername;
    private String mPassword;
    private String mServiceName;
    private XMPPTCPConnection mConnection;
    private ConnectionState mConnectionState;
    private PingManager pingManager;
    private ChatManager chatManager;
    private Roster mRoster;
    private VCardManager vCardManager;
    private String serverurl = "localhost";

    public enum ConnectionState {
        OFFLINE, CONNECTING, ONLINE
    }

    public ConnectionState getmConnectionState() {
        return mConnectionState;
    }

    public void setmConnectionState(ConnectionState mConnectionState) {
        this.mConnectionState = mConnectionState;
    }

    public String getConnectionStateString() {
        switch (mConnectionState) {
            case OFFLINE:
                return "Offline";
            case CONNECTING:
                return "Connecting...";
            case ONLINE:
                return "Online";
            default:
                return "Offline";
        }
    }

    private void updateActivitiesOfConnectionStateChange(ConnectionState mConnectionState) {
        ConnectionState connectionState = mConnectionState;
        String status;
        switch (mConnectionState) {
            case OFFLINE:
                status = "Offline";
                break;
            case CONNECTING:
                status = "Connecting...";
                break;
            case ONLINE:
                status = "Online";
                break;
            default:
                status = "Offline";
                break;
        }
        Intent i = new Intent(Constants.BroadCastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG);
        i.putExtra(Constants.UI_CONNECTION_STATUS_CHANGE, status);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
    }

    public RoosterConnection(Context mApplicationContext) {
        this.mApplicationContext = mApplicationContext;
    }

    public void translateMessage(final String message, final String contactJid) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        String username = prefs.getString("xmpp_jid", null);
        String language = prefs.getString("language", null);
        if (username != null) {
            username = username.split("@")[0];
        }
        HashMap data = new HashMap();
        data.put("username", username);
        data.put("text", message);
        //String translated_text = message;
        Log.d(LOGTAG, username);
        Log.d(LOGTAG, message);
        VolleyService mVolleyService = new VolleyService(new VolleyCallback() {
            @Override
            public void notifySuccess(JSONObject response) throws JSONException {
                try {
                    JSONObject data = response.getJSONObject("data");
                    String Responsetext = data.getString("text");
                    try {
                        String EncodedString = new String(Responsetext.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                        Log.d(LOGTAG, response.toString());
                        Log.d(LOGTAG, Responsetext);
                        Log.d(LOGTAG, EncodedString);
                        Log.d(LOGTAG, "मस्ते");
                        //EncodedString = "मस्ते";
                        UpdateMessagePanel(EncodedString, contactJid, prefs.getString("xmpp_jid", null));
                    } catch (Exception e) {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void notifyError(JSONObject error) {
                try {
                    Log.d(LOGTAG, "Category updation failed");
                    Log.d(LOGTAG, error.getString("data"));
                    UpdateMessagePanel(message, contactJid, prefs.getString("xmpp_jid", null));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, mApplicationContext);
        mVolleyService.postDataVolley(POST_TRANSLATION_TEXT, data);
    }

    public void UpdateMessagePanel(String messageBody, String contactJid, String fromContactJid) {
        ChatMessagesModel.get(mApplicationContext).addMessage(new ChatMessage(messageBody, System.currentTimeMillis(), ChatMessage.Type.RECEIVED, contactJid, fromContactJid));
        if (ContactModel.get(mApplicationContext).isContactStranger(contactJid)) {
            List<com.scalability4all.sathi.model.Chat> chats = ChatModel.get(mApplicationContext).getChatsByJid(contactJid, fromContactJid);
            if (chats.size() == 0) {
                Log.d(LOGTAG, contactJid + " neuer Chat, Timestamp :" + Utilities.getFormattedTime(System.currentTimeMillis()));
                com.scalability4all.sathi.model.Chat chatRooster = new com.scalability4all.sathi.model.Chat(contactJid, fromContactJid, messageBody, com.scalability4all.sathi.model.Chat.ContactType.ONE_ON_ONE, System.currentTimeMillis(), 0);
                ChatModel.get(mApplicationContext).addChat(chatRooster);
                Intent intent = new Intent(Constants.BroadCastMessages.UI_NEW_CHAT_ITEM);
                intent.setPackage(mApplicationContext.getPackageName());
                mApplicationContext.sendBroadcast(intent);
            }
        }
        Intent intent = new Intent(Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
        Log.d(LOGTAG, "package name :" + mApplicationContext.getPackageName());
        intent.setPackage(mApplicationContext.getPackageName());
        intent.putExtra("JabberId", contactJid);
        mApplicationContext.sendBroadcast(intent);
    }

    public void connect() throws IOException, XMPPException, SmackException {
        mConnectionState = ConnectionState.CONNECTING;
        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);
        gatherCredentials();
        InetAddress addr = InetAddress.getByName(mApplicationContext.getResources().getString(R.string.xmpp_host));
        HostnameVerifier verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return false;
            }
        };
        XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(serverurl)
                .setHost(mApplicationContext.getResources().getString(R.string.xmpp_host))
                .setResource("sathi")
                .setKeystoreType(null) //This line seems to get rid of the problem
                .setSendPresence(true)
                .setDebuggerEnabled(true)
                .setHostnameVerifier(verifier)
                .setHostAddress(addr)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setCompressionEnabled(true).build();
        SmackConfiguration.DEBUG = true;
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        mConnection = new XMPPTCPConnection(conf);
        mConnection.setUseStreamManagement(true);
        mConnection.setUseStreamManagementResumption(true);
        mConnection.setPreferredResumptionTime(5);
        mConnection.addConnectionListener(this);
        mRoster = Roster.getInstanceFor(mConnection);
        mRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        mRoster.addSubscribeListener(this);
        mRoster.addRosterListener(this);
        setServerSettings();
        vCardManager = VCardManager.getInstanceFor(mConnection);
        chatManager = ChatManager.getInstanceFor(mConnection);
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                Log.d(LOGTAG, "message.getBody() :" + message.getBody());
                Log.d(LOGTAG, "message.getFrom() :" + message.getFrom());
                String messageSource = message.getFrom().toString();
                String contactJid = "";
                if (messageSource.contains("/")) {
                    contactJid = messageSource.split("/")[0];
                    Log.d(LOGTAG, "The real jid is :" + contactJid);
                    Log.d(LOGTAG, "The message is from :" + from);
                } else {
                    contactJid = messageSource;
                }
                String messageBody = message.getBody();
                translateMessage(messageBody, contactJid);
            }
        });
        ServerPingWithAlarmManager.getInstanceFor(mConnection).setEnabled(true);
        pingManager = PingManager.getInstanceFor(mConnection);
        pingManager.setPingInterval(30);
        try {
            Log.d(LOGTAG, "Calling connect() ");
            mConnection.connect();
            mConnection.login(mUsername, mPassword);
            Log.d(LOGTAG, " login() Called ");
            syncContactListWithRemoteRoster();
            saveUserAvatarsLocaly();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configure XMPP connection to use provided ping timeout and reply timeout.
     */
    private void setServerSettings() {
        // Enable auto-connect
        ReconnectionManager.getInstanceFor(mConnection).enableAutomaticReconnection();
        // Set reconnection policy to increasing delay
        ReconnectionManager.getInstanceFor(mConnection)
                .setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);
    }

    public boolean setSelfAvatar(byte[] image) {
        VCard vCard = new VCard();
        vCard.setAvatar(image);
        if (vCardManager != null) {
            try {
                vCardManager.saveVCard(vCard);
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
                return false;
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
                return false;
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public void saveUserAvatarsLocaly() {
        Log.d(LOGTAG, "Profilbild wird lokal gespeichert");
        File rootPath = new File(mApplicationContext.getExternalFilesDir("DIRECTORY_PICTURES"), "profile_pictures");
        if (!rootPath.exists()) {
            if (rootPath.mkdirs()) {
                Log.d(LOGTAG, "Directory erstellt :" + rootPath.getAbsolutePath());
            } else {
                Log.d(LOGTAG, "Directory konnte nicht erstellt werden :" + rootPath.getAbsolutePath());
            }
        }
        String selfJid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getString("xmpp_jid", null);
        if (selfJid != null) {
            Log.d(LOGTAG, "Valide sid :" + selfJid);
            VCard vCard = null;
            try {
                vCard = vCardManager.loadVCard();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (vCard != null) {
                saveAvatarToDisk(vCard, rootPath, selfJid);
            }
        } else {
            Log.d(LOGTAG, "Self jid NULL");
        }
        List<String> contacts = ContactModel.get(mApplicationContext).getContactsJidStrings();
//        for ( String contact : contacts)
//        {
//            VCard  vCard = getUserVCard(contact);
//            if(vCard!=null)
//            {
//                saveAvatarToDisk(vCard,rootPath,contact);
//            }
//        }
    }

    private void saveAvatarToDisk(VCard vCard, File rootPath, String contact) {
        String imageMimeType = null;
        String imageExtension = null;
        Bitmap.CompressFormat format = null;
        if (vCard != null) {
            byte[] image_data = vCard.getAvatar();
            imageMimeType = vCard.getAvatarMimeType();
            if (image_data != null) {
                Log.d(LOGTAG, "Avatar gefunden : " + contact);
                if (imageMimeType.equals("image/jpeg")) {
                    Log.d(LOGTAG, "Bild JPEG");
                    imageExtension = "jpeg";
                    format = Bitmap.CompressFormat.JPEG;
                } else if (imageMimeType.equals("image/jpg")) {
                    Log.d(LOGTAG, "Bild JPG");
                    imageExtension = "jpg";
                    format = Bitmap.CompressFormat.JPEG;
                } else if (imageMimeType.equals("image/png")) {
                    Log.d(LOGTAG, "Bild PNG");
                    imageExtension = "png";
                    format = Bitmap.CompressFormat.PNG;
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(image_data, 0, image_data.length);
                Bitmap bpResized = Bitmap.createScaledBitmap(bitmap, 70, 70, false);
                File file = new File(rootPath, contact + "." + imageExtension);
                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bpResized.compress(format, 90, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(LOGTAG, "Bild erfolgreich geschrieben :" + file.getAbsolutePath());
            } else {
                Log.d(LOGTAG, "Avatar nicht möglich : " + contact);
            }
        }
    }

    public VCard getUserVCard(String user) {
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(user);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        VCard vCard = null;
        if (vCardManager != null) {
            try {
                vCard = vCardManager.loadVCard(jid);
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return vCard;
    }

    public String getProfileImageAbsolutePath(String jid) {
        File rootPath = new File(mApplicationContext.getExternalFilesDir("DIRECTORY_PICTURES"), "profile_pictures");
        if (!rootPath.exists()) {
            if (rootPath.mkdirs()) {
                Log.d(LOGTAG, "Directory erstellt :" + rootPath.getAbsolutePath());
            } else {
                Log.d(LOGTAG, "Directory konnte nicht erstellt werden :" + rootPath.getAbsolutePath());
            }
        }
        File file = new File(rootPath, jid + ".jpeg");
        if (!file.exists()) {
            file = new File(rootPath, jid + ".jpg");
            if (!file.exists()) {
                file = new File(rootPath, jid + ".png");
                if (!file.exists()) {
                    return null;
                } else {
                    return file.getAbsolutePath();
                }
            } else {
                return file.getAbsolutePath();
            }
        } else {
            return file.getAbsolutePath();
        }
    }

    public void syncContactListWithRemoteRoster() {
        Log.d(LOGTAG, "Roster SYNCING...");
        Collection<RosterEntry> entries = getRosterEntries();
        Log.d(LOGTAG, "Roster von Server " + entries.size() + " Kontakte in Rooster");
        for (RosterEntry entry : entries) {
            RosterPacket.ItemType itemType = entry.getType();
            Log.d(LOGTAG, "Eintrag " + entry.getJid() + " hat subscription " + entry.getType());
            List<String> contacts = ContactModel.get(mApplicationContext).getContactsJidStrings();
            if ((!contacts.contains(entry.getJid().toString())) && (itemType != RosterPacket.ItemType.none)) {
                if (ContactModel.get(mApplicationContext).addContact(new Contact(entry.getJid().toString(),
                        rosterItemTypeToContactSubscriptionType(itemType)))) {
                    Log.d(LOGTAG, "Neuer Kontakt " + entry.getJid().toString() + "hinzugefügt");
                } else {
                    Log.d(LOGTAG, "Kontakt konnte nicht hinzugefügt werden " + entry.getJid().toString());
                }
            }
            if ((contacts.contains(entry.getJid().toString()))) {
                Contact.SubscriptionType subscriptionType = rosterItemTypeToContactSubscriptionType(itemType);
                boolean isSubscriptionPending = entry.isSubscriptionPending();
                Contact mContact = ContactModel.get(mApplicationContext).getContactByJidString(entry.getJid().toString());
                mContact.setPendingTo(isSubscriptionPending);
                mContact.setSubscriptionType(subscriptionType);
                ContactModel.get(mApplicationContext).updateContactSubscription(mContact);
            }
        }
    }

    public Collection<RosterEntry> getRosterEntries() {
        Collection<RosterEntry> entries = mRoster.getEntries();
        Log.d(LOGTAG, "Benutzer hat " + entries.size() + " Kontakte in Roster");
        return entries;
    }

    public void disconnect() {
        Log.d(LOGTAG, "Disconnect Server " + mServiceName);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        prefs.edit().putBoolean("xmpp_logged_in", false).commit();
        if (mConnection != null) {
            mConnection.disconnect();
        }
    }

    public void sendMessage(String body, String toJid, String fromJid) {
        Log.d(LOGTAG, "Nachricht wird gesendet zu: :" + toJid);
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(toJid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        Chat chat = chatManager.chatWith(jid);
        try {
            Message message = new Message(jid, Message.Type.chat);
            message.setBody(body);
            chat.send(message);
            ChatMessagesModel.get(mApplicationContext).addMessage(new ChatMessage(body, System.currentTimeMillis(), ChatMessage.Type.SENT, toJid, fromJid));
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean addContactToRoster(String contactJid) {
        Jid jid;
        try {
            jid = JidCreate.from(contactJid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return false;
        }
        try {
            mRoster.createEntry(jid.asBareJid(), "", null);
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
            return false;
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Contact.SubscriptionType rosterItemTypeToContactSubscriptionType(RosterPacket.ItemType itemType) {
        if (itemType == RosterPacket.ItemType.none) {
            return Contact.SubscriptionType.NONE;
        } else if (itemType == RosterPacket.ItemType.from) {
            return Contact.SubscriptionType.FROM;
        } else if (itemType == RosterPacket.ItemType.to) {
            return Contact.SubscriptionType.TO;
        } else if (itemType == RosterPacket.ItemType.both) {
            return Contact.SubscriptionType.BOTH;
        } else
            return Contact.SubscriptionType.NONE;
    }

    public boolean subscribe(String contact) {
        Jid jidTo = null;
        try {
            jidTo = JidCreate.from(contact);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return false;
        }
        Presence subscribe = new Presence(jidTo, Presence.Type.subscribe);
        return sendPresense(subscribe);
    }

    public boolean unsubscribe(String contact) {
        Jid jidTo = null;
        try {
            jidTo = JidCreate.from(contact);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return false;
        }
        Presence unsubscribe = new Presence(jidTo, Presence.Type.unsubscribe);
        return sendPresense(unsubscribe);
    }

    public boolean unsubscribed(String contact) {
        Jid jidTo = null;
        try {
            jidTo = JidCreate.from(contact);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return false;
        }
        Presence unsubscribed = new Presence(jidTo, Presence.Type.unsubscribed);
        return sendPresense(unsubscribed);
    }

    public boolean subscribed(String contact) {
        Jid jidTo = null;
        try {
            jidTo = JidCreate.from(contact);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return false;
        }
        Presence subscribe = new Presence(jidTo, Presence.Type.subscribed);
        sendPresense(subscribe);
        return true;
    }

    public boolean removeRosterEntry(String contactJid) {
        Jid jid;
        try {
            jid = JidCreate.from(contactJid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return false;
        }
        RosterEntry entry = mRoster.getEntry(jid.asBareJid());
        try {
            mRoster.removeEntry(entry);
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
            return false;
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendPresense(Presence presence) {
        if (mConnection != null) {
            try {
                mConnection.sendStanza(presence);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    private void gatherCredentials() {
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getString("xmpp_jid", null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getString("xmpp_password", null);
        if (jid != null) {
            mUsername = jid.split("@")[0];
            mServiceName = jid.split("@").length > 1 ? jid.split("@")[1] : "";
        } else {
            mUsername = "";
            mServiceName = "";
        }
    }

    private void notifyUiForConnectionError() {
        Intent i = new Intent(Constants.BroadCastMessages.UI_CONNECTION_ERROR);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
    }

    @Override
    public void connected(XMPPConnection connection) {
        Log.d(LOGTAG, "Connected");
        mConnectionState = ConnectionState.CONNECTING;
        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        mConnectionState = ConnectionState.ONLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.ONLINE);
        Log.d(LOGTAG, "Authenticated");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        prefs.edit().putBoolean("xmpp_logged_in", true).commit();
        Intent i = new Intent(Constants.BroadCastMessages.UI_AUTHENTICATED);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
    }

    @Override
    public void connectionClosed() {
        Log.d(LOGTAG, "connectionClosed");
        notifyUiForConnectionError();
        mConnectionState = ConnectionState.OFFLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.OFFLINE);
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(LOGTAG, "connectionClosedOnError");
        notifyUiForConnectionError();
        mConnectionState = ConnectionState.OFFLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.OFFLINE);
    }

    @Override
    public void reconnectionSuccessful() {
        Log.d(LOGTAG, "reconnectionSuccessful");
        mConnectionState = ConnectionState.ONLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.ONLINE);
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.d(LOGTAG, "Reconnecting in " + seconds + "seconds");
        mConnectionState = ConnectionState.CONNECTING;
        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.d(LOGTAG, "reconnectionFailed");
        mConnectionState = ConnectionState.OFFLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.OFFLINE);
    }

    @Override
    public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        String username = prefs.getString("xmpp_jid", null);
        if (!ContactModel.get(mApplicationContext).isContactStranger(from.toString())) {
            Log.d(LOGTAG, "Kontakt kein Unbekannter");
            Contact mContact = ContactModel.get(mApplicationContext).getContactByJidString(from.toString());
            mContact.setPendingFrom(true);
            ContactModel.get(mApplicationContext).updateContactSubscription(mContact);
        } else {
            List<com.scalability4all.sathi.model.Chat> chats = ChatModel.get(mApplicationContext).getChatsByJid(from.toString(), username);
            if (chats.size() == 0) {
                if (ChatModel.get(mApplicationContext).addChat(new com.scalability4all.sathi.model.Chat(from.toString(), username, "Subscription Request", STRANGER,
                        System.currentTimeMillis(), 1))) {
                    Log.d(LOGTAG, "Chat für unbekannten " + from.toString() + " hinzugefügt");
                }
            }
        }
        return null;
    }

    @Override
    public void entriesAdded(Collection<Jid> addresses) {
        for (Jid jid : addresses) {
            RosterEntry entry = mRoster.getEntry(jid.asBareJid());
            RosterPacket.ItemType itemType = entry.getType();
            boolean isSubscriptionPending = entry.isSubscriptionPending();
            List<String> contacts = ContactModel.get(mApplicationContext).getContactsJidStrings();
            if ((!contacts.contains(entry.getJid().toString()))
                    && (itemType != RosterPacket.ItemType.none)) {
                Contact mContact = new Contact(entry.getJid().toString(), rosterItemTypeToContactSubscriptionType(itemType));
                mContact.setPendingTo(isSubscriptionPending);
                if (ContactModel.get(mApplicationContext).addContact(mContact)) {
                    Log.d(LOGTAG, "Neuer Kontakt " + entry.getJid().toString() + "hinzugefügt");
                } else {
                    Log.d(LOGTAG, "Kontakt konnte nicht hinzugefügt werden " + entry.getJid().toString());
                }
            }
            if ((contacts.contains(entry.getJid().toString()))) {
                Contact.SubscriptionType subscriptionType = rosterItemTypeToContactSubscriptionType(itemType);
                Contact mContact = ContactModel.get(mApplicationContext).getContactByJidString(entry.getJid().toString());
                mContact.setPendingTo(isSubscriptionPending);
                mContact.setSubscriptionType(subscriptionType);
                ContactModel.get(mApplicationContext).updateContactSubscription(mContact);
            }
        }
    }

    @Override
    public void entriesUpdated(Collection<Jid> addresses) {
        for (Jid jid : addresses) {
            RosterEntry entry = mRoster.getEntry(jid.asBareJid());
            RosterPacket.ItemType itemType = entry.getType();
            boolean isSubscriptionPending = entry.isSubscriptionPending();
            List<String> contacts = ContactModel.get(mApplicationContext).getContactsJidStrings();
            if ((contacts.contains(entry.getJid().toString()))) {
                Contact.SubscriptionType subscriptionType = rosterItemTypeToContactSubscriptionType(itemType);
                Contact mContact = ContactModel.get(mApplicationContext).getContactByJidString(entry.getJid().toString());
                mContact.setPendingTo(isSubscriptionPending);
                mContact.setSubscriptionType(subscriptionType);
                ContactModel.get(mApplicationContext).updateContactSubscription(mContact);
            }
        }
    }

    @Override
    public void entriesDeleted(Collection<Jid> addresses) {
        for (Jid jid : addresses) {
            if (!ContactModel.get(mApplicationContext).isContactStranger(jid.toString())) {
                Contact mContact = ContactModel.get(mApplicationContext).getContactByJidString(jid.toString());
                if (ContactModel.get(mApplicationContext).deleteContact(mContact)) {
                    Log.d(LOGTAG, "Kontakt " + jid.toString() + " aus Datenbank entfernt");
                }
            }
        }
    }

    @Override
    public void presenceChanged(Presence presence) {
        Presence mPresence = mRoster.getPresence(presence.getFrom().asBareJid());
        Contact mContact = ContactModel.get(mApplicationContext).getContactByJidString(presence.getFrom().asBareJid().toString());
        if (mPresence.isAvailable() && (!mPresence.isAway())) {
            mContact.setOnlineStatus(true);
        } else {
            mContact.setOnlineStatus(false);
        }
        ContactModel.get(mApplicationContext).updateContactSubscription(mContact);
        Intent intent = new Intent(Constants.BroadCastMessages.UI_ONLINE_STATUS_CHANGE);
        intent.putExtra(Constants.ONLINE_STATUS_CHANGE_CONTACT, presence.getFrom().asBareJid().toString());
        intent.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(intent);
    }
}