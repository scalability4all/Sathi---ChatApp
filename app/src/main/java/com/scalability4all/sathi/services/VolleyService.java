package com.scalability4all.sathi.services;

import android.content.Context;
import android.icu.util.Freezable;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.VolleyError;
import com.android.volley.ParseError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.NetworkResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class VolleyService {
    VolleyCallback mResultCallback = null;
    Context mContext;

    public static String BASE_SERVER_IP_ADDRESS="http://34.93.240.242:4567/";
    public static String GET_USER_DETAILS=BASE_SERVER_IP_ADDRESS+"user/details";
    public static String UPDATE_USER_PREFERENCE_LANGUAGE=BASE_SERVER_IP_ADDRESS+"update/user";
    public static String UPDATE_USER_PREFERENCE_CATEGORY=BASE_SERVER_IP_ADDRESS+"update/category";
    public static  String POST_TRANSLATION_TEXT=BASE_SERVER_IP_ADDRESS+"translate";

    public VolleyService(VolleyCallback resultCallback, Context  context){
        mResultCallback = resultCallback;
        mContext = context;
    }

    public void postDataVolley( String url, final HashMap object){
        try {
            RequestQueue requstQueue = Volley.newRequestQueue(mContext);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject responseObject = new JSONObject(new JSONObject(response).getString("response"));
                        if(responseObject.getString("status").equals("ok")) {
                            mResultCallback.notifySuccess(responseObject);
                        } else {
                            mResultCallback.notifyError(responseObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                   Log.d("Volley error", String.valueOf(error));
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json; charset=utf-8");
                    return params;
                }

                @Override
                public byte[] getBody()  {
                    try{
                        return new JSONObject((Map) object).toString().getBytes("utf-8");
                    }
                    catch (Exception e){
                        return new JSONObject((Map) object).toString().getBytes();
                    }

                }
                @Override
                public String getBodyContentType() {
                    return "application/json; charset:utf-8";
                }
                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    try {
                        // solution 1:
                        String jsonString = new String(response.data, "UTF-8");
                        return Response.success(jsonString,
                                HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException e) {
                        return Response.error(new ParseError(e));
                    } catch (Exception je) {
                        return Response.error(new ParseError(je));
                    }
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(
                    100000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requstQueue.add(request);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void getDataVolley(String url){
        try {
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject responseObject = new JSONObject(new JSONObject(response).getString("response"));
                                if(responseObject.getString("status").equals("ok")) {
                                    mResultCallback.notifySuccess(responseObject);
                                } else {
                                    mResultCallback.notifyError(responseObject);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Volley error", String.valueOf(error));
                        }
                    });
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);
            requestQueue.add(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
