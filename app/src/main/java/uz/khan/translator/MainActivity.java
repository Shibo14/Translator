package uz.khan.translator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;
    TextView destinationLanguageTv;
    Button translatorBtn, destinationLanguageChooseBtn, sourceLanguageChooseBtn;
    EditText sourceLanguageEt;

    TranslatorOptions translatorOptions;
    Translator translator;
    ProgressDialog progressDialog;
    ArrayList<ModelLanguage> languageArrayList;

    private static final String TAG = "MAIN_TAG";

    String sourceLanguageCode = "en";
    String sourceLanguageTitle = "English";
    String destinationLanguageCode = "ru";
    String destinationLanguageTitle = "Russian";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        broadcastReceiver = new NetworkBroadcast();
        registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        sourceLanguageEt = findViewById(R.id.sourceLanguageEt);
        destinationLanguageTv = findViewById(R.id.destinationLanguageTv);
        translatorBtn = findViewById(R.id.translatorBtn);
        destinationLanguageChooseBtn = findViewById(R.id.destinationLanguageChooseBtn);
        sourceLanguageChooseBtn = findViewById(R.id.sourceLanguageChooseBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        loadAvailableLanguage();

        sourceLanguageChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sourceLanguageChoose();

            }
        });

        destinationLanguageChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destinationLanguageChoose();
            }
        });
        translatorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });


    }

    String sourceLanguageText = "";

    private void validateData() {
        sourceLanguageText = sourceLanguageEt.getText().toString().trim();
        if (sourceLanguageText.isEmpty()) {
            Toast.makeText(this, "Enter text to translate ... ", Toast.LENGTH_SHORT).show();
        } else {
            startTranslate();
        }

    }

    private void startTranslate() {
        progressDialog.show();
        translatorOptions = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(destinationLanguageCode)
                .build();
        translator = Translation.getClient(translatorOptions);

        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        progressDialog.setMessage("Translate...");

                        translator.translate(sourceLanguageText)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        progressDialog.dismiss();
                                        destinationLanguageTv.setText(s);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        progressDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Failed to translate to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Failed to ready model due to "+e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });


    }

    private void sourceLanguageChoose() {
        PopupMenu popupMenu = new PopupMenu(this, sourceLanguageChooseBtn);
        for (int i = 0; i < languageArrayList.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE, i, i, languageArrayList.get(i).languageTitle);
        }
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int position = menuItem.getItemId();

                sourceLanguageCode = languageArrayList.get(position).languageCode;
                sourceLanguageTitle = languageArrayList.get(position).languageTitle;

                sourceLanguageChooseBtn.setText(sourceLanguageTitle);
                sourceLanguageEt.setHint("Enter " + sourceLanguageTitle);


                return false;
            }
        });
    }


    private void destinationLanguageChoose() {
        PopupMenu popupMenu = new PopupMenu(this, destinationLanguageChooseBtn);

        for (int i = 0; i < languageArrayList.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE, i, i, languageArrayList.get(i).languageTitle);
        }
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int position = menuItem.getItemId();

                destinationLanguageCode = languageArrayList.get(position).languageCode;
                destinationLanguageTitle = languageArrayList.get(position).languageTitle;

                destinationLanguageChooseBtn.setText(destinationLanguageTitle);


                return false;
            }
        });

    }

    private void loadAvailableLanguage() {

        languageArrayList = new ArrayList<>();
        List<String> languageCodeList = TranslateLanguage.getAllLanguages();

        for (String languageCode : languageCodeList) {
            String languageTitle = new Locale(languageCode).getDisplayLanguage();
            Log.d(TAG, "loadAvailableLanguage " + languageCode);
            Log.d(TAG, "loadAvailableLanguage " + languageTitle);

            ModelLanguage modelLanguage = new ModelLanguage(languageCode, languageTitle);
            languageArrayList.add(modelLanguage);
        }


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcastReceiver);
    }
}