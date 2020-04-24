package com.scalability4all.sathi;

import com.android.volley.VolleyError;

import org.json.JSONObject;

interface VolleyCallback {
    void notifySuccess(JSONObject response);

    void notifyError(VolleyError error);
}
abstract  class VolleyCb implements VolleyCallback {

}