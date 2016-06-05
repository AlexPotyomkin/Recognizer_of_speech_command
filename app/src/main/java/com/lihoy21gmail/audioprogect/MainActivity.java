package com.lihoy21gmail.audioprogect;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class MainActivity extends Activity {
    private final String TAG = "myLogs";
    private int myBufferSize;
    private int sampleRate;
    private short[] myBuffer;
    private boolean isReading = false;
    private SpeechCommandRecognizer speechCommandRecognizer;
    private AudioRecord audioRecord = null;
    private Model mModel;
    private SharedPreferences sPref;
    private Boolean MusicState;
    MediaPlayer mediaPlayer;
    AudioManager am;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main1);
        LoadPref();
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mModel = Model.getInstance();
        myBufferSize = mModel.getMyBufferSize();
        sampleRate = mModel.getSampleRate();
        myBuffer = new short[myBufferSize];
        speechCommandRecognizer = new SpeechCommandRecognizer(sampleRate, myBufferSize, getApplicationContext());
        createAudioRecorder();
        //Fragment controlPanel = new ControlPanel();
        //Fragment graphicalPanel = new GraphicalPanel1();
        speechCommandRecognizer.load_standards(getApplicationContext());
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        //GameWorldFragment gameWorldFragment = GameWorldFragment.newInstance(0);
        MainMenuFragment mainMenuFragment = new MainMenuFragment();
        getFragmentManager().
                beginTransaction().
                replace(android.R.id.content, mainMenuFragment).
                commit();


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        if (MusicState) StartMusic();
    }

    @Override
    protected void onStart() {
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.setLooping(true);
        super.onStart();
    }

    private void createAudioRecorder() {
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int minInternalBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                channelConfig, audioFormat);
        int internalBufferSize = minInternalBufferSize * 80;
        Log.d(TAG, "minInternalBufferSize = " + minInternalBufferSize
                + ", internalBufferSize = " + internalBufferSize
                + ", myBufferSize = " + myBufferSize);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfig, audioFormat, internalBufferSize);
        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)
            Log.d(TAG, "SUCCESS");
        else
            Log.d(TAG, "ERROR");

        audioRecord.setPositionNotificationPeriod(myBufferSize);
        //audioRecord.setNotificationMarkerPosition(myBufferSize);

        audioRecord.setRecordPositionUpdateListener(
            new AudioRecord.OnRecordPositionUpdateListener() {
                @Override
                public void onPeriodicNotification(AudioRecord recorder) {

                    speechCommandRecognizer.setRawSignal(myBuffer);

                    class Recognizing extends AsyncTask<Void,Void,Integer>{
                        @Override
                        protected Integer doInBackground(Void... params) {
                            speechCommandRecognizer.clearWordArray();
                            return speechCommandRecognizer.word_selection();
                        }

                        @Override
                        protected void onPostExecute(Integer integer) {
                            super.onPostExecute(integer);
                            if(speechCommandRecognizer.getArray_of_words().size()!=0)
                                mModel.setArray_of_words(speechCommandRecognizer.getArray_of_words());
                            if (integer != -1) {
                                Fragment currentFragment = getFragmentManager().
                                        findFragmentById(android.R.id.content);
                                //Log.d(TAG, "onPeriodicNotification: result != -1");
                                if (currentFragment instanceof GameWorldFragment) {
                                    //Log.d(TAG, "onPeriodicNotification: currentFragment instanceof GameWorldFragment");
                                    mModel.setSpeechRecognitionResult(integer);
                                    mModel.setChangeResult(true);
                                }
                            }
                            //speechCommandRecognizer.clearWordArray();
                        }
                    }
                    Recognizing recogn = new Recognizing();
                    recogn.execute();

                }

                @Override
                public void onMarkerReached(AudioRecord recorder) {}
            }

        );
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onPause: stop record, save standard");
        speechCommandRecognizer.save_standards(getApplicationContext());
        if (audioRecord != null) {
            isReading = false;
            audioRecord.release();
            audioRecord = null;
        }
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

    public void on_Calibrate(int number_of_command) {
        speechCommandRecognizer.Calibrate(number_of_command);
        speechCommandRecognizer.save_standards(getApplicationContext());
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
        isReading = false;
        if (audioRecord != null) {
            try {
                audioRecord.release();
                audioRecord = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void startSpeechRecognition() {
        Log.d(TAG, "startSpeechRecognition: ");

        if (audioRecord == null)
            createAudioRecorder();
        audioRecord.startRecording();
        Thread mThread = new Thread() {
            public void run() {
                Log.d(TAG, "record start, read");
                int count;
                isReading = true;
                while (isReading) {
                    count = audioRecord.read(myBuffer, 0, myBufferSize);
                    mModel.setMyBuffer(myBuffer);
                    if (count != myBufferSize)
                        Log.d(TAG, "Readed not all date" + count);
                }
            }
        };
        mThread.start();

    }
}