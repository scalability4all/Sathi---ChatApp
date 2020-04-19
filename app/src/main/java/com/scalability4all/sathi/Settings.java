package com.scalability4all.sathi;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Settings extends AppCompatActivity  {
    private static final String LOGTAG = "Settings";
    private String languageSelected;
    private String[] newsCategoriesSelected;
    private CharSequence[] languages = {"English", "Hindi", "Telugu"};
    private CharSequence[] newsCategories = {"Business","Politics","Entertainment", "Fashion", "Education"};
    private EditText language;
    private EditText newsCategory;
    private String selectedLanguage;
    private List<CharSequence>  selectedNewsCategories;
    List<CharSequence>  cs = new ArrayList<CharSequence>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        setContentView(R.layout.activity_settings);

        // default
        selectedLanguage="English";
        selectedNewsCategories = new ArrayList<CharSequence>();;
        for(int i=0;i<newsCategories.length;i++) {
            selectedNewsCategories.add(newsCategories[i]);
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
                                    language.setText(selectedLanguage);
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
                                    // TODO Do something

                                    //Dismiss once everything is OK.
                                    dialog.dismiss();
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
                                    newsCategory.setText(selectedNewsCategories.toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ","));
                                    Log.d(LOGTAG,"Selected Categories->" + selectedNewsCategories.toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ","));
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

                                    //Dismiss once everything is OK.
                                    dialog.dismiss();
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