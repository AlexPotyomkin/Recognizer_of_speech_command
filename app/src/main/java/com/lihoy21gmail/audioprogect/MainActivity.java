package com.lihoy21gmail.audioprogect;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;


public class MainActivity extends Activity {
    private final String TAG = "myLogs";
    private AudioRecord audioRecord = null;
    private static final int myBufferSize = 4000;
    private int sampleRate = 8000;
    private boolean isReading = false;
    private short[] myBuffer = new short[myBufferSize];
    private int M = 40;
    private double fl = 300;
    private double fh = 8000;
    private TimeSeries timeSeries;
    private static XYMultipleSeriesDataset dataset;
    private static XYMultipleSeriesRenderer renderer;
    private static XYSeriesRenderer rendererSeries;
    private static GraphicalView view;
    private static Thread mThread;
    private List<short[]> array_of_words = new ArrayList<>();
    private boolean incomplete = false;
    //private


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        createAudioRecorder();

        dataset = new XYMultipleSeriesDataset();
        renderer = new XYMultipleSeriesRenderer();
        rendererSeries = new XYSeriesRenderer();
        timeSeries = new TimeSeries("");

        renderer.addSeriesRenderer(rendererSeries);
        renderer.setBackgroundColor(Color.BLACK);
        renderer.setApplyBackgroundColor(true);
        renderer.setXLabelsColor(Color.WHITE);
        renderer.setYLabelsColor(0, Color.WHITE);
        //renderer.setYLabelsColor(Color.BLACK);
        rendererSeries.setColor(Color.GREEN);
        renderer.setAxesColor(Color.WHITE);
        renderer.setShowLegend(false);
        renderer.setXAxisMax(myBufferSize);//
        renderer.setXAxisMin(0);
        dataset.addSeries(timeSeries);
        //view = ChartFactory.getBarChartView(this, dataset, renderer, BarChart.Type.DEFAULT);
        view = ChartFactory.getTimeChartView(this, dataset, renderer, "");

        setContentView(view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (audioRecord == null)
            createAudioRecorder();
        audioRecord.startRecording();


        mThread = new Thread() {
            public void run() {
                Log.d(TAG, "record start, read");
                int count;
                isReading = true;
                while (isReading) {
                    count = audioRecord.read(myBuffer, 0, myBufferSize);
                    if(count !=myBufferSize)
                        Log.d(TAG, "Readed not all date" + count);
                }
            }
        };
        mThread.start();


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

        audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                    word_selection();
            }
            @Override
            public void onMarkerReached (AudioRecord recorder){
                Log.d(TAG, "onMarkerReached");

/*
            if (audioRecord != null) {
                Log.d(TAG, "onMarkerReached: clear recorder");
                isReading = false;
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }*/
            }
            }

            );
        }

    void word_selection() {
        short P = 200;
        int k = 0, t = (int) (sampleRate * 0.3);
        int min_length = (int) (sampleRate * 0.3),
                max_length = (int) (sampleRate), word_size;
        int begin_word, l = 0;

        Log.d(TAG, "count word = " + array_of_words.size());
        if (incomplete) {
            incomplete = false;
            // копирую слова в впромежуточный буфер для удобства
            word_size = array_of_words.get(array_of_words.size() - 1).length;
            short temp[] = new short[word_size];
            System.arraycopy(array_of_words.get(array_of_words.size() - 1), 0,
                    temp, 0, word_size);
                    /*// ищу сколько отсчетов меньше порога были в буфере
                    while (l != word_size) {
                        if (Math.abs(temp[l]) > P) k = 0;
                        else k++;
                        l++;
                    }*/
            l = 0;
            // ищу количество отсчетов меньше порога в потоке
            while (k < t) {
                if (Math.abs(myBuffer[l]) > P) k = 0;
                else k++;
                if (l + 1 < myBufferSize) l++;
                else {
                    // Если дошел весе окно потока, но не нашел конца
                    // то выхожу из цикла, поднимаю флаг
                    incomplete = true;
                    break;
                }
            }

            int new_word_size = word_size + (l - t);
            // Если флаг поднят, то размер слова равен старое + длинна потока
            if (incomplete)
                new_word_size = word_size + l;
            // Елси найден конец или дошел до конца потока, то записую в список слово
            if (l > t || incomplete) {

                // Если размер слова не привышает максимальной длинны, то записую в списоок
                if (new_word_size <= max_length) {
                    short new_word[] = new short[new_word_size];
                    System.arraycopy(temp, 0, new_word, 0, word_size);
                    System.arraycopy(myBuffer, 0, new_word, word_size, new_word_size - word_size);
                    array_of_words.set(array_of_words.size() - 1, new_word);
                    Log.d(TAG, "set new word, length = " +
                            array_of_words.get(array_of_words.size() - 1).length);
                } else {
                    array_of_words.remove(array_of_words.size() - 1);
                    incomplete = false;
                    Log.d(TAG, "not decent, length = " + word_size + " new_word_size = " +
                            new_word_size + " max_length" + max_length + " min_length" +
                            min_length);
                }
            } else if (new_word_size < min_length) {
                array_of_words.remove(array_of_words.size() - 1);
                incomplete = false;
                Log.d(TAG, "not decent, word size = " + word_size + "new word size" +
                        new_word_size + " to small" + "min_length = " + min_length);
            }
        }
        // Цикл поиска начала слова если флаг не поднят
        for (int i = l; i < myBufferSize; i++) {
            k = 0;
            if (Math.abs(myBuffer[i]) > P) {
                begin_word = i;
                if (i + 1 < myBufferSize) i++;
                // Циул поиска конца слова
                while (k <= t) {
                    if (Math.abs(myBuffer[i]) > P) k = 0;
                    else k++;
                    if (i + 1 < myBufferSize) i++;
                    else {
                        incomplete = true;
                        break;
                    }
                }
                // Слово записывается в список, если подходит по размерам или незакончено
                if (incomplete) word_size = i - begin_word;
                else word_size = i - begin_word - t;

                if ((min_length < word_size && word_size < max_length) ||
                        (word_size < max_length && incomplete)) {
                    short new_word[] = new short[word_size];
                    System.arraycopy(myBuffer, begin_word, new_word, 0, word_size);
                    array_of_words.add(new_word);
                    Log.d(TAG, "add new word  " + array_of_words.size() + ", length = " +
                            array_of_words.get(array_of_words.size() - 1).length);
                } else {
                    Log.d(TAG, "not decent, length = " + word_size);
                    incomplete = false;
                }
            }
        }
        //silence_removing();





        // Обновление графика
        if (timeSeries.getItemCount() != 0)
            timeSeries.clear();

        if (!incomplete && array_of_words.size() != 0) {
            word_size = array_of_words.get(array_of_words.size() - 1).length;
            double new_word[] = new double[word_size];
            for(int i=0; i<word_size;i++)
                new_word[i] = array_of_words.get(array_of_words.size() - 1)[i];
            //System.arraycopy(array_of_words.get(array_of_words.size() - 1), 0, new_word, 0, word_size);
            RealDoubleFFT transformer = new RealDoubleFFT(word_size);
            transformer.ft(new_word);
            //Log.d(TAG, "word_selection: word size = " + word_size + "  temp size = " + (word_size/2));

            double temp[] = new double[word_size];
            for (int i = 0; i < word_size; i++) {
                temp[i] = Math.abs(new_word[i]);
            }
            double MFCC[] = C(temp);


            for (int i = 0; i < M; i++) {
                timeSeries.add(i, MFCC[i]);
                Log.d(TAG, "MFCC[" + i + "]= " + MFCC[i]);
                renderer.setXAxisMax(M);
            }
            view.repaint();
        }

    }

    void silence_removing() {
        int P = 10;
        for (int j = 0; j < array_of_words.size(); j++) {
            int k = 0;
            for (short inner_item : array_of_words.get(j))
                if (Math.abs(inner_item) < P)
                    k++;
            int new_word_size = array_of_words.get(j).length - k;
            short new_word[] = new short[new_word_size];
            int l = 0;
            for (short item : array_of_words.get(j))
                if (Math.abs(item) > P)
                    new_word[l++] = item;
            Log.d(TAG, "silence_removing: change word size: " + new_word_size);
            array_of_words.set(j, new_word);
        }
    }

    /*List<short[]> division_into_frames(short [] arr_data){
        List<short[]> array_of_frame = new ArrayList<>();

        return array_of_frame;
    }*/

    double M2F(double mel){
        return 700*(Math.exp(mel/1125)-1);
    }

    double F2M(double freq){
        return 1125*Math.log(1+ freq/700);
    }

    double f(int m){
        return ((double) myBufferSize/sampleRate) * M2F(F2M(fl) + m * ((F2M(fh)-F2M(fl))/(M+1)));
    }

    double H(int m, double k){
        if(f(m-1)<=k && k<=f(m)){
            //Log.d(TAG, "H: 1" + " k = " +k +" m = " +m+ "  f(m) = "+ f(m) + " f(m-1)" + f(m-1) + "  f(m+1)" +f(m+1)  );
            return (k-f(m-1))/(f(m)-f(m-1));
            }
        if(f(m)<=k && k<=f(m+1)) {
          //  Log.d(TAG, "H: 1" + " k = " +k +" m = " +m+ "  f(m) = "+ f(m) + " f(m-1)" + f(m-1) + "  f(m+1)" +f(m+1)  );
            return (f(m + 1) - k) / (f(m + 1) - f(m));
        }
        //Log.d(TAG, "H: 1" + " k = " +k +" m = " +m+ "  f(m) = "+ f(m) + " f(m-1)" + f(m-1) + "  f(m+1)" +f(m+1)  );
            return 0;
    }

    double[] C(double[] data){
        Log.d(TAG, "C: data = " +data.length);
        double s[] = new double[M];
        for(int i = 0; i < M; i++) {
            for (int j=0; j<data.length;j++) {
                s[i] += Math.pow(data[j], 2) * H(i, j);
            }
            s[i] = Math.log(s[i]);
            Log.d(TAG, "C: s["+i+"] = " + s[i]);
        }
        double c[] = new double[M];
        for(int i = 0; i < M; i++)
            for (int j = 0; j < M; j++) {
                c[i] += s[j] * Math.cos((Math.PI * i * (j + 0.5)) / M);
            }
        return c;
    }

    @Override
    protected void onStop () {
        super.onStop();
        Log.d(TAG, "onPause: stop record");
        if (audioRecord != null) {
            isReading = false;
            audioRecord.release();
            audioRecord = null;
        }
    }
}

