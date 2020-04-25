package com.scalability4all.sathi;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.scalability4all.sathi.services.VolleyCallback;
import com.scalability4all.sathi.services.VolleyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.scalability4all.sathi.services.VolleyService.UPDATE_USER_PREFERENCE_CATEGORY;
import static com.scalability4all.sathi.services.VolleyService.UPDATE_USER_PREFERENCE_LANGUAGE;

public class Settings extends AppCompatActivity  {
    private static final String LOGTAG = "Settings";
    private String languageSelected;
    private String[] newsCategoriesSelected;
    private CharSequence[] languages = {"english", "hindi", "telugu"};
    private CharSequence[] newsCategories = {"Business","Politics","Entertainment", "Fashion", "Education"};
    private EditText language;
    private EditText newsCategory;
    private String selectedLanguage;
    private List<CharSequence>  selectedNewsCategories;
    List<CharSequence>  cs = new ArrayList<CharSequence>();
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        setContentView(R.layout.activity_settings);

        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username=sh.getString("xmpp_jid",null);
        if(username!=null) {
            username=username.split("@")[0];
        }

        selectedLanguage = sh.getString("language",null);
        String newsCategorySavedInDb=sh.getString("category",null);
        if(newsCategorySavedInDb!=null && newsCategorySavedInDb.length()>0) {
            selectedNewsCategories = new ArrayList<CharSequence>(Arrays.asList(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("category",null).split(",")));
        } else {
            selectedNewsCategories = new ArrayList<CharSequence>();
        }


        language=(EditText)findViewById(R.id.language);
        language.setInputType(InputType.TYPE_NULL);
        language.setText(selectedLanguage);
        language.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //dialogue popup
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this)
                            .setTitle(R.string.choose_language)
                            .setPositiveButton(R.string.save, null)
                            .setNeutralButton(R.string.cancel, null)
                            .setSingleChoiceItems(languages, Arrays.asList(languages).indexOf(selectedLanguage) , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    selectedLanguage=new String((String) languages[i]);
                                    Log.d(LOGTAG,"Selected Language->" + selectedLanguage);
                                }
                            });
                    final AlertDialog dialog=alertDialogBuilder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {

                            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    language.setText(selectedLanguage);
                                    HashMap data = new HashMap();
                                    data.put("username", username);
                                    data.put("language",selectedLanguage);
                                    VolleyService mVolleyService = new VolleyService(new VolleyCallback() {
                                        @Override
                                        public void notifySuccess(JSONObject response) throws JSONException {
                                            try {
                                                JSONObject data=new JSONObject(response.getString("data"));
                                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Settings.this);
                                                prefs.edit()
                                                        .putString("language", data.getString("language"))
                                                        .commit();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            dialog.dismiss();
                                        }
                                        @Override
                                        public void notifyError(JSONObject error) {
                                            try {
                                                Log.d(LOGTAG,"Language updation failed");
                                                Log.d(LOGTAG,error.getString("data"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },Settings.this);
                                    mVolleyService.postDataVolley(UPDATE_USER_PREFERENCE_LANGUAGE,data);
                                }
                            });
                        }
                    });
                    dialog.show();
                }
                return false;
            }
        });

        newsCategory=(EditText)findViewById(R.id.news_category);
        newsCategory.setInputType(InputType.TYPE_NULL);
        newsCategory.setText(selectedNewsCategories.toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ","));
        newsCategory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //dialogue popup
                    boolean[] newsChioces =new boolean[newsCategories.length];
                    for (int i = 0; i < newsCategories.length; i++)
                        newsChioces[i] = selectedNewsCategories.indexOf(newsCategories[i])==-1 ? false : true;
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this)
                            .setTitle(R.string.category_choose_stmt)
                            .setPositiveButton(R.string.save, null)
                            .setNeutralButton(R.string.cancel, null)
                            .setMultiChoiceItems(newsCategories, newsChioces, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    if(isChecked) {
                                        selectedNewsCategories.add(newsCategories[which]);
                                    } else {
                                        selectedNewsCategories.remove(newsCategories[which]);
                                    }
                                }
                            });
                    final AlertDialog dialog=alertDialogBuilder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                        @Override
                        public void onShow(DialogInterface dialogInterface) {

                            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    newsCategory.setText(selectedNewsCategories.toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ","));
                                    Log.d(LOGTAG,"Selected Categories->" + selectedNewsCategories.toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ","));
                                    HashMap data = new HashMap();
                                    data.put("username",username);
                                    data.put("category",selectedNewsCategories.toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ","));
                                    VolleyService mVolleyService = new VolleyService(new VolleyCallback() {
                                        @Override
                                        public void notifySuccess(JSONObject response) throws JSONException {
                                            try {
                                                JSONArray data=new JSONArray(response.getString("data"));
                                                List<CharSequence> list = new ArrayList<CharSequence>();
                                                for(int i = 0; i < data.length(); i++){
                                                    list.add((CharSequence) data.get(i));
                                                }
                                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Settings.this);
                                                prefs.edit()
                                                        .putString("category", list.toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ","))
                                                        .commit();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            dialog.dismiss();
                                        }
                                        @Override
                                        public void notifyError(JSONObject error) {
                                            try {
                                                Log.d(LOGTAG,"Category updation failed");
                                                Log.d(LOGTAG,error.getString("data"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },Settings.this);
                                    mVolleyService.postDataVolley(UPDATE_USER_PREFERENCE_CATEGORY,data);
                                }
                            });
                        }
                    });
                    dialog.show();
                }
                return false;
            }
        });

    }
}