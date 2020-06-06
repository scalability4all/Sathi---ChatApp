package com.scalability4all.sathi.services;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public interface LanguagesListCallback {
    void notifySuccess(Map<CharSequence, String> response) throws JSONException;

    void notifyError(JSONObject error);

}
