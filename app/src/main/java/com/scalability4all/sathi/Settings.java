package com.scalability4all.sathi;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.scalability4all.sathi.services.VolleyCallback;
import com.scalability4all.sathi.services.VolleyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.scalability4all.sathi.services.VolleyService.UPDATE_USER_PREFERENCE_CATEGORY;
import static com.scalability4all.sathi.services.VolleyService.UPDATE_USER_PREFERENCE_LANGUAGE;

public class Settings extends AppCompatActivity {
    private static final String LOGTAG = "Settings";
    private CharSequence[] languages;
    private CharSequence[] newsCategories = { "Business", "Politics", "Entertainment", "LifeStyle", "India", "Sports",
            "World" };
    private EditText language;
    private TableLayout newsCategory;
    private String selectedLanguage;
    private List<CharSequence> selectedNewsCategories;
    List<CharSequence> cs = new ArrayList<CharSequence>();
    String username;
    Map<CharSequence, String> languages_locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        setContentView(R.layout.activity_settings);
        newsCategory = (TableLayout) findViewById(R.id.news_category);

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = sh.getString("xmpp_jid", null);
        if (username != null) {
            username = username.split("@")[0];
        }

        languages_locale = Constants.languages_locale;

        languages = languages_locale.keySet().toArray(new CharSequence[0]);

        selectedLanguage = sh.getString("language", null);
        String newsCategorySavedInDb = sh.getString("category", null);
        if (newsCategorySavedInDb != null && newsCategorySavedInDb.length() > 0) {
            selectedNewsCategories = new ArrayList<CharSequence>(Arrays.asList(PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext()).getString("category", null).split(",")));
        } else {
            selectedNewsCategories = new ArrayList<CharSequence>();
        }

        addNewsCategories(newsCategory);

        language = findViewById(R.id.language);
        language.setInputType(InputType.TYPE_NULL);
        language.setText(selectedLanguage);
        language.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // dialogue popup
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this)
                            .setTitle(R.string.choose_language).setPositiveButton(R.string.save, null)
                            .setNeutralButton(R.string.cancel, null).setSingleChoiceItems(languages,
                                    Arrays.asList(languages).indexOf(selectedLanguage),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            selectedLanguage = (String) languages[i];
                                            Log.d(LOGTAG, "Selected Language->" + selectedLanguage);
                                        }
                                    });
                    final AlertDialog dialog = alertDialogBuilder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {

                            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    language.setText(selectedLanguage);
                                    HashMap data = new HashMap();
                                    data.put("username", username);
                                    data.put("language", languages_locale.get(selectedLanguage));
                                    VolleyService mVolleyService = new VolleyService(new VolleyCallback() {
                                        @Override
                                        public void notifySuccess(JSONObject response) throws JSONException {
                                            try {
                                                JSONObject data = new JSONObject(response.getString("data"));
                                                SharedPreferences prefs = PreferenceManager
                                                        .getDefaultSharedPreferences(Settings.this);
                                                prefs.edit().putString("language", selectedLanguage).commit();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void notifyError(JSONObject error) {
                                            try {
                                                Log.d(LOGTAG, "Language updation failed");
                                                Log.d(LOGTAG, error.getString("data"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, Settings.this);
                                    mVolleyService.postDataVolley(UPDATE_USER_PREFERENCE_LANGUAGE, data);
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

    private void addNewsCategories(LinearLayout linearLayout) {

        TableRow row = new TableRow(getApplicationContext());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);

        for (int i = 0; i < newsCategories.length; i++) {

            if (i != 0 && i % 2 == 0) {
                row = new TableRow(getApplicationContext());
                row.setLayoutParams(lp);
            }

            CheckBox checkBox = new CheckBox(getApplicationContext());
            checkBox.setTag(newsCategories[i]);
            checkBox.setText(newsCategories[i]);

            if (selectedNewsCategories.indexOf(newsCategories[i]) != -1) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Is the view now checked?
                    boolean checked = ((CheckBox) view).isChecked();
                    if (checked)
                        selectedNewsCategories.add(((CheckBox) view).getText());
                    else
                        selectedNewsCategories.remove(((CheckBox) view).getText());

                    saveNewsCategories();

                }
            });

            row.addView(checkBox);
            
            // to make sure each row has two checkboxes, and a new row is created after the row is filled.
            if (i % 2 == 0) {
                linearLayout.addView(row);
            }else if(i+1<newsCategories.length){
                row = new TableRow(getApplicationContext());
                row.setLayoutParams(lp);
            }
        }
    }

    private void saveNewsCategories() {
        HashMap data = new HashMap();
        data.put("username", username);
        data.put("category", selectedNewsCategories.toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ","));
        VolleyService mVolleyService = new VolleyService(new VolleyCallback() {
            @Override
            public void notifySuccess(JSONObject response) throws JSONException {
                try {
                    JSONArray data = new JSONArray(response.getString("data"));
                    List<CharSequence> list = new ArrayList<CharSequence>();
                    for (int i = 0; i < data.length(); i++) {
                        list.add((CharSequence) data.get(i));
                    }
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Settings.this);
                    prefs.edit().putString("category", list.toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ","))
                            .commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void notifyError(JSONObject error) {
                try {
                    Log.d(LOGTAG, "Category updation failed");
                    Log.d(LOGTAG, error.getString("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, Settings.this);
        mVolleyService.postDataVolley(UPDATE_USER_PREFERENCE_CATEGORY, data);
    }

}