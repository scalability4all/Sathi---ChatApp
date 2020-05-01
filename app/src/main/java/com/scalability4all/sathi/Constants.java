package com.scalability4all.sathi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Constants {
    public static Map<CharSequence, String> languages_locale;


    public static final class BroadCastMessages
    {
        public static final String UI_AUTHENTICATED = "com.scalability4all.sathi.uiauthenticated";
        public static final String UI_CONNECTION_ERROR = "com.scalability4all.sathi.ui_connection_error";
        public static final String UI_CONNECTION_STATUS_CHANGE_FLAG = "com.scalability4all.sathi.connection_status_change_flag";
        public static final String UI_NEW_MESSAGE_FLAG = "com.scalability4all.sathi.ui_new_message_flag";
        public static final String UI_NEW_CHAT_ITEM = "com.scalability4all.sathi.ui_new_chat_item";
        public static final String UI_ONLINE_STATUS_CHANGE = "com.scalability4all.sathi.ui_online_status_change";
    }
    public static final String UI_CONNECTION_STATUS_CHANGE = "com.scalability4all.sathi.connection_status_change";
    public static final String UI_NEW_MESSAGE = "com.scalability4all.sathi.ui_new_message";
    public  static final String ONLINE_STATUS_CHANGE_CONTACT = "com.scalability4all.sathi.online_status_change_contact";

    static {
        // languages
        languages_locale= new HashMap<>();
        languages_locale.put("english", "en-IN");
        languages_locale.put("hindi", "hi-IN");
        languages_locale.put("punjabi", "pa-guru-IN");
        languages_locale.put("tamil", "ta-IN");
        languages_locale.put("telugu", "te-IN");
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}