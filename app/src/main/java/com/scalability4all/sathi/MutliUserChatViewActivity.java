package com.scalability4all.sathi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;


// Gruppenchat ohne Funktion, nähere Details im Projektbericht
public class MutliUserChatViewActivity extends AppCompatActivity {
    public MultiUserChat mMultiUserChat;
    private MultiUserChatManager mMultiUserChatManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutli_user_chat_view);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");
        String roomname = intent.getStringExtra("roomname");
        String roompass = intent.getStringExtra("roompass");

        mMultiUserChatManager = MultiUserChatManager.getInstanceFor(getConnection());
        mMultiUserChatManager.addInvitationListener(new InvitationListener() {
            @Override
            public void invitationReceived(XMPPConnection conn, MultiUserChat room, EntityJid inviter, String reason, String password, Message message, MUCUser.Invite invitation) {

            }
        });

        try {
            mMultiUserChat = mMultiUserChatManager.getMultiUserChat((EntityBareJid) JidCreate.from(roomname));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        mMultiUserChat.addMessageListener(new MessageListener() {
            @Override
            public void processMessage(Message message) {

            }
        });

        try {
            try {
                mMultiUserChat.join(Resourcepart.from(username), password);
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            }
        } catch (SmackException.NoResponseException | org.jxmpp.stringprep.XmppStringprepException | InterruptedException | SmackException.NotConnectedException | MultiUserChatException.NotAMucServiceException e) {
            e.printStackTrace();
        }
    }

    // XMPPConnection ohne Logindaten
    public XMPPConnection getConnection() {
        try {
            String serverurl = "saar-force.de";
            XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(serverurl)
                    .setHost(serverurl)
                    .setResource(serverurl)
                    .setKeystoreType(null)
                    .setSendPresence(true)
                    .setDebuggerEnabled(true)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setCompressionEnabled(true).build();
            SmackConfiguration.DEBUG = true;
            XMPPTCPConnection.setUseStreamManagementDefault(true);
            final AbstractXMPPConnection mConnection = new XMPPTCPConnection(conf);
            return mConnection;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return null;
        }
    }
}
