package com.lihoy21gmail.audioprogect;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;


public class MainActivity extends FragmentActivity {
    private final String TAG = "myLogs";
    private int myBufferSize;
    private int sampleRate;
    private short[] myBuffer;
    private boolean isReading = false;
    private SpeechCommandRecognizer speechCommandRecognizer;
    private AudioRecord audioRecord = null;
    private Model mModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main1);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mModel = Model.getInstance();
        myBufferSize = mModel.getMyBufferSize();
        sampleRate = mModel.getSampleRate();
        myBuffer = new short[myBufferSize];
        speechCommandRecognizer = new SpeechCommandRecognizer(sampleRate, myBufferSize, getApplicationContext());
        createAudioRecorder();
        Fragment controlPanel = new ControlPanel();
        Fragment graphicalPanel = new GraphicalPanel1();
        speechCommandRecognizer.load_standards(getApplicationContext());

        LoadLevel(2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*Log.d(TAG, "onResume: ");
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
        mThread.start();*/
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
                        speechCommandRecognizer.word_selection();
                    }
                    @Override
                    public void onMarkerReached(AudioRecord recorder) {
                    }
                }
        );
    }

    void LoadLevel(int lvl){
        GameWorldFragment gameWorldFragment = GameWorldFragment.newInstance(lvl);
        //gameWorldFragment.setArguments(lvl);
        getFragmentManager().
                beginTransaction().
                replace(android.R.id.content, gameWorldFragment).
                commit();
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
    }

    public void on_Calibrate(int number_of_command) {
        speechCommandRecognizer.Calibrate(number_of_command);
        speechCommandRecognizer.save_standards(getApplicationContext());
    }
}

