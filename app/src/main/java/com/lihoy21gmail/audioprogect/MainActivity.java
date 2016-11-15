package com.lihoy21gmail.audioprogect;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;


public class MainActivity extends Activity implements
        RecognitionListener {
    private final String TAG = "myLogs";
    private Model mModel;
    private SharedPreferences sPref;
    private Boolean MusicState;
    private MediaPlayer mediaPlayer;
    private AudioManager am;
    private boolean flag;
    private static final String COMMANDS_SEARCH = "commands";
    private SpeechRecognizer recognizer;
    private int lastLength = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        LoadPref();
        Log.d("", "onCreate: " + Locale.getDefault().getLanguage());
        switch (Locale.getDefault().getLanguage()) {
            case "ru":
                flag = true;
                break;
            case "en":
                flag = false;
                break;
            default:
                flag = false;
        }
        mModel = Model.getInstance();
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        MainMenuFragment mainMenuFragment = new MainMenuFragment();
        getFragmentManager().
                beginTransaction().
                replace(android.R.id.content, mainMenuFragment).
                commit();
    }

    @Override
    protected void onStart() {
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.setLooping(true);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (MusicState) StartMusic();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onPause: stop record");
        if (MusicState)
            mediaPlayer.stop();
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        StopMusic();
        super.onPause();
    }

    public void StartMusic() {
        mediaPlayer.start();
    }

    public void StopMusic() {
        if (mediaPlayer != null)
            if (mediaPlayer.isPlaying())
                mediaPlayer.pause();
    }

    public void LoadPref() {
        Log.d(TAG, "LoadPref: main_activity");
        sPref = getBaseContext().getSharedPreferences("settings", 0);
        SharedPreferences.Editor ed = sPref.edit();
        MusicState = sPref.getBoolean("MusicState", false);
        ed.apply();
    }

    public void stopSpeechRecognition() {
        Log.d(TAG, "stopSpeechRecognition: ");
        if(recognizer!= null)
        recognizer.stop();
    }

    public void startSpeechRecognition() {
        Log.d(TAG, "startSpeechRecognition: ");
        Toast.makeText(this, getResources().getString(R.string.pre_warn),
                Toast.LENGTH_SHORT).show();
        runRecognizerSetup();
    }

    private void runRecognizerSetup() {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.d(TAG, "Failed to init recognizer " + result);
                } else {
                    reset();
                }
            }
        }.execute();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

        if (hypothesis == null)
            return;
        char text[] = hypothesis.getHypstr().toCharArray();
        char temp[] = "\0".toCharArray();
        if (text.length != 0 && lastLength != text.length) {
            lastLength = text.length;
            for (int i = 0; i < text.length; i++)
                if (text[i] == ' ') {
                    temp = new char[i];
                    System.arraycopy(text, 0, temp, 0, i);
                    break;
                }
            mModel.setSpeechRecognitionResult(changeResult(String.valueOf(temp)));
        }
    }

    public int changeResult(String str) {
        Log.d(TAG, "Result: " + str);
        switch (str) {
            case "направо":
            case "right":
                return 0;
            case "налево":
            case "left":
                return 1;
            case "вверх":
            case "up":
                return 2;
            case "вниз":
            case "down":
                return 3;
            case "отмена":
            case "cancel":
                return 4;
            case "меню":
            case "menu":
                return 5;
        }
        return -1;
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
    }

    private void reset() {
        recognizer.stop();
        recognizer.startListening(COMMANDS_SEARCH);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        File commandstxt;
        if (!flag) {
            recognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                    .setDictionary(new File(assetsDir, "cmudict-en-us-short.dict"))
                    .setBoolean("-allphone_ci", true)
                    .getRecognizer();
            recognizer.addListener(this);
            commandstxt = new File(assetsDir, "commands-en.txt");
            recognizer.addKeywordSearch(COMMANDS_SEARCH, commandstxt);
        } else {
            recognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(assetsDir, "zero_ru.cd_ptm_4000"))
                    //.setAcousticModel(new File(assetsDir, "cmusphinx-ru-5.2"))
                    .setDictionary(new File(assetsDir, "cmudict-ru-short.dic"))
                    .setBoolean("-allphone_ci", true)
                    .getRecognizer();
            recognizer.addListener(this);
            commandstxt = new File(assetsDir, "commands-ru.txt");
            recognizer.addKeywordSearch(COMMANDS_SEARCH, commandstxt);
        }

    }

    @Override
    public void onError(Exception error) {
    }

    @Override
    public void onTimeout() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

}