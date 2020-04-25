package com.scalability4all.sathi.services;

import org.json.JSONException;
import org.json.JSONObject;

public interface VolleyCallback {
    void notifySuccess(JSONObject response) throws JSONException;

    void notifyError(JSONObject error);
}
