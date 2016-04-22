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
import java.util.zip.DeflaterOutputStream;


public class MainActivity extends Activity {
    private final String TAG = "myLogs";
    private AudioRecord audioRecord = null;
    private static final int myBufferSize = 8000;
    private int sampleRate = 8000;
    private boolean isReading = false;
    private short[] myBuffer = new short[myBufferSize];
    private int M = 16;
    private double fl = 100;
    private double fh = 4000;
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

        renderer.setAxisTitleTextSize(40);
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
/*
                if (timeSeries.getItemCount() != 0)
                    timeSeries.clear();

                RealDoubleFFT transformer = new RealDoubleFFT(myBufferSize);
                //Log.d(TAG, "word_selection: fft_size = " +word_size);
                double myBufferD [] = new double[myBufferSize];
                for (int i =0; i<myBufferSize;i++)
                    myBufferD[i] = myBuffer[i];
                transformer.ft(myBufferD);


                double kof = ((double) myBufferSize/sampleRate)*0.5;
                for (int i = 0; i < myBufferSize; i++) {

                    if (i * kof < fl || i * kof > fh){
                        myBufferD[i] = 0;
                    }
                }
                for (int i = 0; i < myBufferSize; i++) {
                    timeSeries.add(i, myBufferD[i]);
                    renderer.setXAxisMax(myBufferSize);
                }
                view.repaint();
*/
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
        if (!incomplete && array_of_words.size() != 0) {
            if (timeSeries.getItemCount() != 0)
                timeSeries.clear();
            word_size = array_of_words.get(array_of_words.size() - 1).length;
            double new_word[] = new double[word_size];
            for(int i=0; i<word_size;i++)
                new_word[i] = array_of_words.get(array_of_words.size() - 1)[i];
            //System.arraycopy(array_of_words.get(array_of_words.size() - 1), 0, new_word, 0, word_size);
            RealDoubleFFT transformer = new RealDoubleFFT(word_size);
            Log.d(TAG, "word_selection: fft_size = " +word_size);
            transformer.ft(new_word);
            //Log.d(TAG, "word_selection: word size = " + word_size + "  temp size = " + (word_size/2));

            double kof = ((double) myBufferSize/sampleRate)*0.5;

            for (int i = 0; i < word_size; i++)
                if(i*kof<fl&&i*kof>fh)
                    new_word[i] = 0;

            List<double[]> array_of_frame = division_into_frames(new_word,6);
            List<double[]> array_of_MFCC = new ArrayList<>();
            double mfcc [];
            //Log.d(TAG, "array size = " +array_of_frame.size());
            for(int i = 0; i< array_of_frame.size(); i++) {
                mfcc = C(array_of_frame.get(i));
                //Log.d(TAG, "array size = " +array_of_frame.size());
                //Log.d(TAG, "mfcc geted "+ i);
                array_of_MFCC.add(mfcc);
                array_of_frame.remove(i);
            }

            /*
            double MFCC1[] = C(temp1);
            double MFCC2[] = C(temp2);

            for (int i = 0; i < M*2; i++) {
                if(i<M) {
                    timeSeries.add(i, MFCC1[i]);
                    Log.d(TAG, "MFCC[" + i + "]= " + MFCC1[i] + ";");
                }
                else  {
                    timeSeries.add(i, MFCC1[i-M]);
                    Log.d(TAG, "MFCC[" + i + "]= " + MFCC2[i-M] + ";");
                }

                renderer.setXAxisMax(M*2);
            }
            view.repaint();
*/
            //associate(MFCC);
            Log.d(TAG, "word_selection: was there");
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

    double M2F(double mel){
        return 700*(Math.exp(mel/1125)-1);
    }

    double F2M(double freq){
        return 1125*Math.log(1+ freq/700);
    }

    double f(int m){
        return ((double) myBufferSize/sampleRate)*0.5 * M2F(F2M(fl) + m * ((F2M(fh)-F2M(fl))/(M+1)));
    }

    double H(int m, double k){
        k = 0.5*k*((double)sampleRate/myBufferSize);
        if(f(m-1)<=k && k<=f(m))
            return (k-f(m-1))/(f(m)-f(m-1));
        if(f(m)<=k && k<=f(m+1))
            return (f(m + 1) - k) / (f(m + 1) - f(m));
        return 0;
    }

    double[] C(double[] data){

        double s[] = new double[M];
        for(int i = 0; i < M; i++) {
            s[i] = 1;
            for (int j=0; j<data.length; j++) {
                s[i] += Math.pow(data[j], 2) * H(i+1, j);
            }
            s[i] = Math.log(s[i]);
        }

        double c[] = new double[M];
        for(int i = 0; i < M; i++)
            for (int j = 0; j < M; j++) {
                c[i] += s[j] * Math.cos((Math.PI * i * (j + 0.5)) / M);
            }
            return c;
    }

    List<double[]> division_into_frames(double [] arr_data, int part){
        List<double[]> array_of_frame = new ArrayList<>();
        double temp[] = new double[1+arr_data.length/part];
        Log.d(TAG, "division_into_frames: ");
        /*for (int i =0; i< arr_data.length; i++) {
            for (int j = 0; j < arr_data.length / part; j++) {
                temp[j] = arr_data[i];
                if ((j + 1) == arr_data.length / part) {
                    array_of_frame.add(temp);
                    Log.d(TAG, "new frame add. size = " + temp.length + "  array_size = " + array_of_frame.size());
                }
            }
        }*/
            for (int i =0; i< part; i++) {
                for (int j = 0; j < arr_data.length / part; j++){
                    temp [j] = arr_data[j+(i*arr_data.length/part)];
                }
                array_of_frame.add(temp);
                Log.d(TAG, "new frame add. size = " + temp.length + "  array_size = " + array_of_frame.size());
            }

        return array_of_frame;
    }

    void associate(double mfcc[] ){
        double command[][] = new double[4][M/2];
        //право
        command[0][0]= 460.0214345880812;
        command[0][1]= 325.60084883392943;
        command[0][2]= 68.51524422704372;
        command[0][3]= -63.128501072079914;
        command[0][4]= -20.444290781873526;
        command[0][5]= 41.91018818384576;
        command[0][6]= 15.811150921698326;
        command[0][7]= -37.92818295274615;
        command[0][8]= -29.54810894272778;
        command[0][9]= 17.668269591732717;
        command[0][10]= 28.566059034493378;
        command[0][11]= 1.0648960209125242;
        command[0][12]= -9.643712925358232;
        command[0][13]= 8.326774156927161;
        command[0][14]= 15.485302175351055;
        command[0][15]= -1.0464617959818998;
        command[0][16]= -9.572744326055044;
        command[0][17]= 3.581061298250644;
        command[0][18]= 11.209220439458683;
        command[0][19]= -2.0220434178614113;
        //лево
        command[1][0]= 431.19831204364164;
        command[1][1]= 305.30488588871054;
        command[1][2]= 60.19158765897813;
        command[1][3]= -73.43787626640787;
        command[1][4]= -37.8931404050834;
        command[1][5]= 32.104727011002254;
        command[1][6]= 24.841488086726397;
        command[1][7]= -21.43921144946878;
        command[1][8]= -22.728926001182995;
        command[1][9]= 15.970511823511048;
        command[1][10]= 30.60272525670404;
        command[1][11]= 7.330738481150874;
        command[1][12]= -9.567865375547138;
        command[1][13]= 0.8067907942091725;
        command[1][14]= 10.216642297169157;
        command[1][15]= -1.0965452118749237;
        command[1][16]= -12.66443340474953;
        command[1][17]= -4.479633666098579;
        command[1][18]= 9.264762294334666;
        command[1][19]= 7.847312206188361;
        //вверх
        command[2][0]= 433.5141480739206;
        command[2][1]= 327.10792210464257;
        command[2][2]= 100.74942570435707;
        command[2][3]= -63.76497928045282;
        command[2][4]= -75.89765360513935;
        command[2][5]= -1.6835105523548233;
        command[2][6]= 40.08163465413855;
        command[2][7]= 12.126287338830748;
        command[2][8]= -27.490742739358982;
        command[2][9]= -20.982936315879538;
        command[2][10]= 18.1404197005701;
        command[2][11]= 36.8980571209872;
        command[2][12]= 16.94505064486603;
        command[2][13]= -9.193625439579757;
        command[2][14]= -8.765020167352114;
        command[2][15]= 10.331739242950245;
        command[2][16]= 18.289077983138643;
        command[2][17]= 6.163713534630363;
        command[2][18]= -7.019062346331911;
        command[2][19]= -4.857883246273197;
        //вниз
        command[3][0]= 463.16864151074884;
        command[3][1]= 329.44493631496294;
        command[3][2]= 62.169346557283156;
        command[3][3]= -94.83504530505019;
        command[3][4]= -59.575630180625325;
        command[3][5]= 36.946238496778996;
        command[3][6]= 50.45945569738243;
        command[3][7]= -8.960449208072765;
        command[3][8]= -37.88347384023853;
        command[3][9]= -1.1621606959367536;
        command[3][10]= 36.9035528295106;
        command[3][11]= 23.47922039866785;
        command[3][12]= -11.623499911654001;
        command[3][13]= -15.082147722582935;
        command[3][14]= 9.673590061798228;
        command[3][15]= 19.57557437438533;
        command[3][16]= 3.2456167651413814;
        command[3][17]= -7.900530976667975;
        command[3][18]= 4.97611763322635;
        command[3][19]= 20.201713395919086;

        double similarity[] = new double[4];

        for (int i =0; i< command.length; i++) {
            for(int j =2; j < 16; j++) {

                double A,B,C,D;
                A = Math.abs(mfcc[j]-command[i][j]);
                B = Math.abs(mfcc[j+1]-command[i][j+1]);
                C = Math.abs(mfcc[j+2]-command[i][j+2]);
                D = Math.abs(mfcc[j+3]-command[i][j+3]);
                similarity[i] += (A+B*2+C*2+D)/2;
/*
                similarity[i] = Math.sqrt(Math.pow(mfcc[j]-command[i][j-2],2)+
                        Math.pow(mfcc[j]-command[i][j-1],2)+
                        Math.pow(mfcc[j]-command[i][j],2)+
                        Math.pow(mfcc[j]-command[i][j+1],2)+
                        Math.pow(mfcc[j]-command[i][j+2],2)) ;*/

            }
        }
        Log.d(TAG, "associate: \n"
                +" command 1 = " + similarity[0]+ "\n"
                +" command 2 = " + similarity[1]+ "\n"
                +" command 3 = " + similarity[2]+ "\n"
                +" command 4 = " + similarity[3]);
        double min=similarity[0];
        int mini=0;
        for(int i=1; i< command.length;i++)
            if(similarity[i]<min) {
                min = similarity[i];
                mini = i;
            }
        switch (mini){
            case 0: renderer.setXTitle("Направо");
                Log.d(TAG, "Направо");
                break;
            case 1: renderer.setXTitle("Налево");
                Log.d(TAG, "Налево");
                break;
            case 2: renderer.setXTitle("Вверх");
                Log.d(TAG, "Вверх");
                break;
            case 3: renderer.setXTitle("Вниз");
                Log.d(TAG, "Вниз");
                break;
        }

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

