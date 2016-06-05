package com.lihoy21gmail.audioprogect;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class SpeechCommandRecognizer {
    private final String TAG = "myLogs";
    private List<short[]> array_of_words = new ArrayList<>();
    private int windowSize;
    private int sampleRate;
    private boolean incomplete = false;
    private int M = 20;
    private int divison_of_parts = 7;
    private double fl = 90;
    private double fh = 5000;
    private double lastMFCC;
    private short rawSignal[];
    private double array_of_MFCC[][] = new double[divison_of_parts][M];
    private List<double[][]> array_of_Command = new ArrayList<>();
    private SharedPreferences sPref;
    private Model mModel;
    private Context mCntxt;

    SpeechCommandRecognizer(int sampleRate, int windowSize, Context cntx) {
        this.sampleRate = sampleRate;
        this.windowSize = windowSize;
        mModel = Model.getInstance();
        mCntxt = cntx;
    }

    public void setRawSignal(short[] data) {
        rawSignal = new short[data.length];
        System.arraycopy(data, 0, rawSignal, 0, data.length);
    }

    public int word_selection() {
        short P = 50;
        int k = 0, t = (int) (sampleRate * 0.3);
        int min_length = (int) (sampleRate * 0.25),
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
                if (Math.abs(rawSignal[l]) > P) k = 0;
                else k++;
                if (l + 1 < windowSize) l++;
                else {
                    // Если дошел до конца потока, но не нашел конца
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
                    System.arraycopy(rawSignal, 0, new_word, word_size, new_word_size - word_size);
                    array_of_words.set(array_of_words.size() - 1, new_word);
                    Log.d(TAG, "set new word, length = " +
                            array_of_words.get(array_of_words.size() - 1).length);
                } else {
                    array_of_words.remove(array_of_words.size() - 1);
                    incomplete = false;
                    //Log.d(TAG, "not decent, length = " + word_size + " new_word_size = " +
                    //        new_word_size + " max_length" + max_length + " min_length" +
                    //       min_length);
                }
            } else if (new_word_size < min_length) {
                array_of_words.remove(array_of_words.size() - 1);
                incomplete = false;
                //Log.d(TAG, "not decent, word size = " + word_size + "new word size" +
                //       new_word_size + " to small" + "min_length = " + min_length);
            }
        }

        // Цикл поиска начала слова, если флаг не поднят
        for (int i = l; i < windowSize; i++) {
            k = 0;
            if (Math.abs(rawSignal[i]) > P) {
                begin_word = i;
                if (i + 1 < windowSize) i++;
                // Циул поиска конца слова
                while (k <= t) {
                    if (Math.abs(rawSignal[i]) > P) k = 0;
                    else k++;
                    if (i + 1 < windowSize) i++;
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
                    System.arraycopy(rawSignal, begin_word, new_word, 0, word_size);
                    array_of_words.add(new_word);
                    Log.d(TAG, "add new word  " + array_of_words.size() + ", length = " +
                            array_of_words.get(array_of_words.size() - 1).length);
                } else {
                    //Log.d(TAG, "not decent, length = " + word_size);
                    incomplete = false;
                }
            }
        }
        //silence_removing();
        //mModel.setArray_of_words(array_of_words);

        // Обновление графика
        if (!incomplete && array_of_words.size() != 0) {
            // Thread mThread = new Thread() {
            // new Handler().postDelayed(new Runnable() {
            //     public void run() {
            word_size = array_of_words.get(array_of_words.size() - 1).length;
            double new_word[] = new double[word_size];
            for (int i = 0; i < word_size; i++)
                new_word[i] = array_of_words.get(array_of_words.size() - 1)[i];

            //System.arraycopy(array_of_words.get(array_of_words.size() - 1), 0, new_word, 0, word_size);

            RealDoubleFFT transformer = new RealDoubleFFT(word_size);
            transformer.ft(new_word);
            //Log.d(TAG, "word_selection: word size = " + word_size + "  temp size = " + (word_size/2));
/*
            double kof = ((double) windowSize / sampleRate) * 0.5;

           for (int i = 0; i < word_size; i++)
                if (i * kof < fl && i * kof > fh)
                    new_word[i] = 0;
*/
            List<double[]> array_of_frame = division_into_frames(new_word, divison_of_parts);
            double mfcc[];
            //Log.d(TAG, "array size = " +array_of_frame.size());
            for (int i = 0; i < divison_of_parts; i++) {
                mfcc = C(array_of_frame.get(i));
                System.arraycopy(mfcc, 0, array_of_MFCC[i], 0, M);
            }
            array_of_frame.clear();

            //if(!incomplete)
             //   array_of_words.clear();
            //},10);
            //mThread.start();
            // mModel.setArray_of_MFCC(array_of_MFCC);
            return associate(array_of_MFCC);
        }
        return -1;
    }

    public void silence_removing() {
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
            array_of_words.set(j, new_word);
        }
    }

    public double M2F(double mel) {
        return 700 * (Math.exp(mel / 1125) - 1);
    }

    public double F2M(double freq) {
        return 1125 * Math.log(1 + freq / 700);
    }

    public double f(int m) {
        return ((double) windowSize / sampleRate) * 0.5 * M2F(F2M(fl) + m * ((F2M(fh) - F2M(fl)) / (M + 1)));
    }

    public double H(int m, double k) {
        k = 0.5 * k * ((double) sampleRate / windowSize);
        if (f(m - 1) <= k && k <= f(m))
            return (k - f(m - 1)) / (f(m) - f(m - 1));
        if (f(m) <= k && k <= f(m + 1))
            return (f(m + 1) - k) / (f(m + 1) - f(m));
        return 0;
    }

    public double[] C(double[] data) {

        double s[] = new double[M];
        for (int i = 0; i < M; i++) {
            s[i] = 1;
            for (int j = 0; j < data.length; j++) {
                s[i] += Math.pow(data[j], 2) * H(i + 1, j);
            }
            s[i] = Math.log(s[i]);
        }

        double c[] = new double[M];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < M; j++) {
                c[i] += s[j] * Math.cos((Math.PI * i * (j + 0.5)) / M);
            }
        return c;
    }

    public List<double[]> division_into_frames(double[] arr_data, int part) {
        List<double[]> array_of_frame = new ArrayList<>();

        for (int i = 0; i < part; i++) {
            double temp[] = new double[1 + arr_data.length / part];
            System.arraycopy(arr_data, i * arr_data.length / part, temp, 0, arr_data.length / part);
            array_of_frame.add(temp);
        }
        return array_of_frame;
    }

    public int associate(double mfcc[][]) {
        double similarity[] = new double[array_of_Command.size()];
        for (int i = 0; i < array_of_Command.size(); i++) {
            for (int j = 0; j < divison_of_parts; j++)
                for (int l = 0; l < M - 3; l++) {
                    similarity[i] += Math.sqrt(Math.abs(Math.pow(mfcc[j][l], 2)
                            - Math.pow(array_of_Command.get(i)[j][l], 2)));
                }
        }

        if(similarity[0]!=lastMFCC) {
            lastMFCC = similarity[0];
            double min = similarity[0];
            int mini = 0;
            for (int i = 1; i < array_of_Command.size(); i++)
                if (similarity[i] < min) {
                    min = similarity[i];
                    mini = i;
                }
            if (min > 25000)
                return -1;
            Log.d(TAG, "associate: \n"
                    + " command 1 = " + similarity[0] + "\n"
                    + " command 2 = " + similarity[1] + "\n"
                    + " command 3 = " + similarity[2] + "\n"
                    + " command 4 = " + similarity[3]);
            switch (mini) {
                case 0:
                    //renderer.setXTitle("Направо");
                    Log.d(TAG, "Направо");
                    //Toast.makeText(mCntxt, "Направо", Toast.LENGTH_SHORT).show();
                    return 0;
                case 1:
                    //renderer.setXTitle("Направо");
                    Log.d(TAG, "Налево");
                    //Toast.makeText(mCntxt, "Налево", Toast.LENGTH_SHORT).show();
                    return 1;
                case 2:
                    //renderer.setXTitle("Направо");
                    Log.d(TAG, "Вверх");
                    //Toast.makeText(mCntxt, "Вверх", Toast.LENGTH_SHORT).show();
                    return 2;
                case 3:
                    //renderer.setXTitle("Направо");
                    Log.d(TAG, "Вниз");
                    // Toast.makeText(mCntxt, "Вниз", Toast.LENGTH_SHORT).show();
                    return 3;
                default:
                    return -1;
            }
        } else
        {
            //Log.d(TAG, "associate: not change");
            return -1;
        }
    }

    public void Calibrate(int number_of_command) {
        double temp[][] = new double[divison_of_parts][M];
        for (int i = 0; i < divison_of_parts; i++)
            for (int j = 0; j < M; j++)
                temp[i][j] = array_of_MFCC[i][j];
        array_of_Command.set(number_of_command, temp);
        Log.d(TAG, "Calibrate " + number_of_command + " done!");
    }

    public void save_standards(Context mContext) {
        sPref = mContext.getSharedPreferences("MyPref", 0);
        Editor ed = sPref.edit();
        for (int i = 0; i < array_of_Command.size(); i++) {
            for (int j = 0; j < divison_of_parts; j++)
                for (int l = 0; l < M; l++)
                    ed.putFloat("command [" + i + "][" + j + "][" + l + "]",
                            (float) array_of_Command.get(i)[j][l]);
            ed.apply();
        }
    }

    public void load_standards(Context mContext) {
        sPref = mContext.getSharedPreferences("MyPref", 0);
        Editor ed = sPref.edit();
        for (int i = 0; i < 4; i++) {
            double temp[][] = new double[divison_of_parts][M];
            for (int j = 0; j < divison_of_parts; j++)
                for (int l = 0; l < M; l++) {
                    temp[j][l] = (double)
                            sPref.getFloat("command [" + i + "][" + j + "][" + l + "]", 0);
                    //Log.d(TAG, "aOc["+i+"]["+j+"]["+l+"] = " +
                    //        sPref.getFloat("command [" + i + "][" + j + "][" + l + "]", 0));
                }
            array_of_Command.add(temp);
        }
        ed.apply();
    }

    public List<short[]> getArray_of_words() {
        if (incomplete)
            return new ArrayList<>();
         else
            return array_of_words;
    }

    public void clearWordArray (){
        if(array_of_words.size()>5)
        array_of_words.remove(0);
    }
}


/*
            double A, B, C, D;
            A = Math.abs(mfcc[j][l] - array_of_Command.get(i)[j][l]);
            B = Math.abs(mfcc[j][l + 1] - array_of_Command.get(i)[j][l + 1]);
            C = Math.abs(mfcc[j][l + 2] - array_of_Command.get(i)[j][l + 2]);
            D = Math.abs(mfcc[j][l + 3] - array_of_Command.get(i)[j][l + 3]);
            similarity[i] += (A + B * 2 + C * 2 + D) / 2;
        }
        similarity[i] = Math.sqrt(similarity[i]);*/
 /*       for (int i = 0; i < command.length; i++) {
            for (int j = 2; j < 16; j++) {

                double A, B, C, D;
                A = Math.abs(mfcc[j] - command[i][j]);
                B = Math.abs(mfcc[j + 1] - command[i][j + 1]);
                C = Math.abs(mfcc[j + 2] - command[i][j + 2]);
                D = Math.abs(mfcc[j + 3] - command[i][j + 3]);
                similarity[i] += (A + B * 2 + C * 2 + D) / 2;
/*
                    similarity[i] = Math.sqrt(Math.pow(mfcc[j][l]-array_of_Command.get(i)[j][l-2],2)+
                            Math.pow(mfcc[j][l]-array_of_Command.get(i)[j][l-1],2)+
                            Math.pow(mfcc[j][l]-array_of_Command.get(i)[j][l],2)+
                            Math.pow(mfcc[j][l]-array_of_Command.get(i)[j][l+1],2)+
                            Math.pow(mfcc[j][l]-array_of_Command.get(i)[j][l+2],2));*/